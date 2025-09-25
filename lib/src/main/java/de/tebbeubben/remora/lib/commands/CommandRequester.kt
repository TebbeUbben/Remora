package de.tebbeubben.remora.lib.commands

import de.tebbeubben.remora.lib.messaging.MessageHandler
import de.tebbeubben.remora.lib.model.commands.RemoraCommand
import de.tebbeubben.remora.lib.model.commands.RemoraCommandData
import de.tebbeubben.remora.lib.model.commands.toModel
import de.tebbeubben.remora.lib.model.commands.toProtobuf
import de.tebbeubben.remora.lib.persistence.repositories.CommandRepository
import de.tebbeubben.remora.proto.messages.CommandProgressMessage
import de.tebbeubben.remora.proto.messages.CommandResultMessage
import de.tebbeubben.remora.proto.messages.PrepareCommandResponseMessage
import de.tebbeubben.remora.proto.messages.confirmCommandMessage
import de.tebbeubben.remora.proto.messages.prepareCommandMessage
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Clock
import kotlin.time.Instant

@Singleton
internal class CommandRequester @Inject constructor(
    private val commandRepository: CommandRepository,
    private val messageHandler: MessageHandler,
) {

    val commandStateFlow get() = commandRepository.stateFlow

    suspend fun clear() {
        commandRepository.save(null)
    }

    suspend fun initiateCommand(data: RemoraCommandData) {
        val command = RemoraCommand.Initial(
            timestamp = Clock.System.now(),
            followerSequenceId = commandRepository.getNewSequenceId(),
            originalData = data,
            lastAttempt = null
        )
        commandRepository.save(command)
        sendPrepareRequest(command)
    }

    suspend fun sendPrepareRequest(command: RemoraCommand.Initial? = null) {
        val cached = command ?: commandRepository.getCached() as? RemoraCommand.Initial ?: return
        messageHandler.sendPrepareCommandMessage(prepareCommandMessage {
            this.followerSequenceId = cached.followerSequenceId
            this.timestamp = Clock.System.now().epochSeconds
            when (cached.originalData) {
                is RemoraCommandData.Bolus -> this.bolusCommand = cached.originalData.toProtobuf()
            }
        })
        commandRepository.save(
            cached.copy(
                lastAttempt = Clock.System.now()
            )
        )
    }

    suspend fun handlePrepareResponse(message: PrepareCommandResponseMessage) {
        val cached = commandRepository.getCached() as? RemoraCommand.Initial ?: return
        if (cached.followerSequenceId != message.followerSequenceId) return
        val commandCase = message.commandCase
        if (commandCase == PrepareCommandResponseMessage.CommandCase.ERROR) {
            commandRepository.save(
                RemoraCommand.Rejected(
                    timestamp = Clock.System.now(),
                    followerSequenceId = cached.followerSequenceId,
                    originalData = cached.originalData,
                    error = message.error.toModel()
                )
            )
        } else {
            val constrainedData = when (commandCase) {
                PrepareCommandResponseMessage.CommandCase.COMMAND_NOT_SET -> return
                PrepareCommandResponseMessage.CommandCase.BOLUS_COMMAND   -> message.bolusCommand.toModel()
                else                                                      -> error("Command not implemented")
            }
            if (constrainedData::class != cached.originalData::class) {
                //TODO: Log
                return
            }
            commandRepository.save(
                RemoraCommand.Prepared(
                    timestamp = Clock.System.now(),
                    followerSequenceId = cached.followerSequenceId,
                    originalData = cached.originalData,
                    mainSequenceId = message.mainSequenceId,
                    constrainedData = constrainedData,
                    validUntil = Instant.fromEpochSeconds(message.validUntil),
                    lastAttempt = null
                )
            )
        }
    }

    suspend fun sendConfirmation() {
        val cached = commandRepository.getCached() as? RemoraCommand.Prepared ?: return
        messageHandler.sendConfirmCommandMessage(confirmCommandMessage {
            this.mainSequenceId = cached.mainSequenceId
            this.timestamp = Clock.System.now().epochSeconds
        })
        commandRepository.save(cached.copy(lastAttempt = Clock.System.now()))
    }

    suspend fun handleProgress(message: CommandProgressMessage) {
        val cached = commandRepository.getCached() ?: return
        if (cached !is RemoraCommand.SecondStage) return
        if (cached is RemoraCommand.Final) return
        if (cached.mainSequenceId != message.mainSequenceId) return
        val timestamp = Instant.fromEpochSeconds(message.timestamp)
        if (cached is RemoraCommand.Progressing && cached.timestamp > timestamp) return
        commandRepository.save(
            RemoraCommand.Progressing(
                timestamp = timestamp,
                followerSequenceId = cached.followerSequenceId,
                originalData = cached.originalData,
                mainSequenceId = cached.mainSequenceId,
                constrainedData = cached.constrainedData,
                progress = when (message.progressCase) {
                    CommandProgressMessage.ProgressCase.CONNECTING_STARTED_AT ->
                        RemoraCommand.Progress.Connecting(Instant.fromEpochSeconds(message.connectingStartedAt))

                    CommandProgressMessage.ProgressCase.PERCENTAGE            -> RemoraCommand.Progress.Percentage(message.percentage)
                    CommandProgressMessage.ProgressCase.IS_ENQUEUED           -> RemoraCommand.Progress.Enqueued
                    CommandProgressMessage.ProgressCase.PROGRESS_NOT_SET      -> error("Progress not set")
                }
            )
        )
    }

    suspend fun handleResult(message: CommandResultMessage) {
        val cached = commandRepository.getCached() ?: return
        if (cached !is RemoraCommand.SecondStage) return
        if (cached is RemoraCommand.Final) return
        if (cached.mainSequenceId != message.mainSequenceId) return
        val result = when (message.commandCase) {
            CommandResultMessage.CommandCase.ERROR           -> RemoraCommand.Result.Error(message.error.toModel())
            CommandResultMessage.CommandCase.BOLUS_COMMAND   -> RemoraCommand.Result.Success(message.bolusCommand.toModel())
            CommandResultMessage.CommandCase.COMMAND_NOT_SET -> return
        }
        if (result is RemoraCommand.Result.Success && result.finalData::class != cached.constrainedData::class) {
            // TODO: Log
            return
        }
        commandRepository.save(
            RemoraCommand.Final(
                timestamp = Instant.fromEpochSeconds(message.timestamp),
                followerSequenceId = cached.followerSequenceId,
                originalData = cached.originalData,
                mainSequenceId = cached.mainSequenceId,
                constrainedData = cached.constrainedData,
                result = result
            )
        )
    }

}