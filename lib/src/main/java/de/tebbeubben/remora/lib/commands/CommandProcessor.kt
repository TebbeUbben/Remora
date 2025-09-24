package de.tebbeubben.remora.lib.commands

import android.os.SystemClock
import de.tebbeubben.remora.lib.messaging.MessageHandler
import de.tebbeubben.remora.lib.model.commands.PreparedCommand
import de.tebbeubben.remora.lib.model.commands.RemoraCommandData
import de.tebbeubben.remora.lib.model.commands.RemoraCommandError
import de.tebbeubben.remora.lib.model.commands.toModel
import de.tebbeubben.remora.lib.model.commands.toProtobuf
import de.tebbeubben.remora.lib.persistence.repositories.CommandRepository
import de.tebbeubben.remora.proto.messages.ConfirmCommandMessage
import de.tebbeubben.remora.proto.messages.PrepareCommandMessage
import de.tebbeubben.remora.proto.messages.commandProgressMessage
import de.tebbeubben.remora.proto.messages.commandResultMessage
import de.tebbeubben.remora.proto.messages.prepareCommandResponseMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

@Singleton
internal class CommandProcessor @Inject constructor(
    private val commandRepository: CommandRepository,
    private val messageHandler: MessageHandler,
) : CoroutineScope by CoroutineScope(Dispatchers.IO) {

    var commandHandler: CommandHandler? = null
    private var mutex = Mutex()
    private var preparedCommand: PreparedCommand? = null
    private var currentCommandJob: Job? = null

    private suspend fun sendPrepareCommandResponse(
        peerId: Long,
        followerSequenceId: Int,
        mainSequenceId: Int = 0,
        validUntil: Instant? = null,
        data: RemoraCommandData? = null,
        error: RemoraCommandError? = null,
    ) {
        messageHandler.enqueuePrepareCommandResponseMessage(peerId, prepareCommandResponseMessage {
            this.followerSequenceId = followerSequenceId
            this.mainSequenceId = mainSequenceId
            this.timestamp = Clock.System.now().epochSeconds
            this.validUntil = validUntil?.epochSeconds ?: 0
            if (error != null) this.error = error.toProtobuf()
            when (data) {
                is RemoraCommandData.Bolus -> this.bolusCommand = data.toProtobuf()
                null                       -> Unit
            }
        })
    }

    suspend fun handlePrepareCommand(peerId: Long, message: PrepareCommandMessage) {
        mutex.withLock {
            if (currentCommandJob?.isActive == true) {
                sendPrepareCommandResponse(peerId, message.followerSequenceId, error = RemoraCommandError.ACTIVE_COMMAND)
                return
            }
            val current = preparedCommand
            if (current != null && current.peerId == peerId && current.followerSequenceId == message.followerSequenceId) {
                sendPrepareCommandResponse(
                    peerId,
                    message.followerSequenceId,
                    current.mainSequenceId,
                    current.validUntil,
                    current.data
                )
                return
            }
            val commandData = when (message.commandCase) {
                PrepareCommandMessage.CommandCase.BOLUS_COMMAND   -> message.bolusCommand.toModel()
                PrepareCommandMessage.CommandCase.COMMAND_NOT_SET -> return
            }
            val handler = commandHandler ?: return
            val result = when (commandData) {
                is RemoraCommandData.Bolus -> handler.prepareBolus(commandData)
            }
            when (result) {
                is CommandHandler.Result.Error<*>                   -> {
                    sendPrepareCommandResponse(peerId, message.followerSequenceId, error = result.error)
                }

                is CommandHandler.Result.Success<RemoraCommandData> -> {
                    val command = PreparedCommand(
                        peerId = peerId,
                        followerSequenceId = message.followerSequenceId,
                        mainSequenceId = commandRepository.getNewSequenceId(),
                        validUntil = Clock.System.now() + PREPARED_COMMAND_VALIDITY,
                        data = result.data
                    )
                    sendPrepareCommandResponse(
                        peerId,
                        message.followerSequenceId,
                        command.mainSequenceId,
                        command.validUntil,
                        result.data
                    )
                    preparedCommand = command
                }
            }
        }
    }

    suspend fun handleConfirmCommand(peerId: Long, message: ConfirmCommandMessage) {
        // TODO: Status snapshot
        mutex.withLock {
            val handler = commandHandler ?: return
            val current = preparedCommand
            if (current === null || current.peerId != peerId || current.mainSequenceId != message.mainSequenceId) {
                sendCommandResultMessage(peerId, message.mainSequenceId, error = RemoraCommandError.WRONG_SEQUENCE_ID)
                return
            }
            if (currentCommandJob?.isActive == true) return
            if (current.validUntil < Clock.System.now()) {
                sendCommandResultMessage(peerId, current.mainSequenceId, error = RemoraCommandError.EXPIRED)
                return
            }
            currentCommandJob = launch {
                try {
                    runCommand(peerId, current.mainSequenceId) {
                        with(handler) {
                            when (current.data) {
                                is RemoraCommandData.Bolus -> executeBolus(current.data)
                            }
                        }
                    }
                } finally {
                    withContext(NonCancellable) {
                        preparedCommand = null
                        currentCommandJob = null
                    }
                }
            }
        }
    }

    private suspend fun sendCommandProgressMessage(peerId: Long, mainSequenceId: Int, percent: Int?) {
        messageHandler.sendCommandProgressMessage(peerId, commandProgressMessage {
            this.mainSequenceId = mainSequenceId
            this.timestamp = Clock.System.now().epochSeconds
            percent?.let { this.percentage = percent }
        })
    }

    private suspend fun sendCommandResultMessage(peerId: Long, mainSequenceId: Int, data: RemoraCommandData? = null, error: RemoraCommandError? = null) {
        messageHandler.enqueueCommandResultMessage(peerId, commandResultMessage {
            this.mainSequenceId = mainSequenceId
            this.timestamp = Clock.System.now().epochSeconds
            when {
                error != null                   -> this.error = error.toProtobuf()
                data is RemoraCommandData.Bolus -> this.bolusCommand = data.toProtobuf()
            }
        })
    }

    @OptIn(FlowPreview::class)
    private suspend fun runCommand(peerId: Long, mainSequenceId: Int, block: suspend CommandHandler.ExecutionScope.() -> CommandHandler.Result<RemoraCommandData>) {
        var lastProgress: Int? = -1
        var lastProgressReport = 0L
        callbackFlow {
            val executionScope = object : CommandHandler.ExecutionScope {
                override suspend fun reportIntermediateProgress(progress: Int?) {
                    send(Progress.Intermediate(progress))
                }
            }
            when (val result = block(executionScope)) {
                is CommandHandler.Result.Error<*>                   -> send(Progress.Error(result.error))
                is CommandHandler.Result.Success<RemoraCommandData> -> send(Progress.Success(result.data))
            }
            channel.close()
            awaitClose()
        }.collectLatest { progress ->
            when (progress) {
                is Progress.Intermediate -> {
                    if (lastProgress == progress.percent) return@collectLatest
                    delay(lastProgressReport - SystemClock.uptimeMillis() - 1000)
                    this@CommandProcessor.launch {
                        try {
                            sendCommandProgressMessage(peerId, mainSequenceId, progress.percent)
                        } catch (e: Exception) {
                            e.printStackTrace()
                            //TODO: Log
                        }
                    }
                    lastProgressReport = SystemClock.uptimeMillis()
                    lastProgress = progress.percent
                }
                is Progress.Error   -> sendCommandResultMessage(peerId, mainSequenceId, error = progress.error)
                is Progress.Success -> sendCommandResultMessage(peerId, mainSequenceId, progress.data)
            }
        }
    }

    sealed class Progress {
        data class Intermediate(val percent: Int?) : Progress()
        data class Error(val error: RemoraCommandError) : Progress()
        data class Success(val data: RemoraCommandData) : Progress()
    }

    companion object {

        val PREPARED_COMMAND_VALIDITY = 5.minutes
    }
}