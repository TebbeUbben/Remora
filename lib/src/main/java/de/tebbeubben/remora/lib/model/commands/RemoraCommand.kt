@file:UseSerializers(InstantComponentSerializer::class)

package de.tebbeubben.remora.lib.model.commands

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.builtins.InstantComponentSerializer
import kotlin.time.Instant

@Serializable
@SerialName("Command")
sealed class RemoraCommand {

    abstract val timestamp: Instant
    abstract val followerSequenceId: Int
    abstract val originalData: RemoraCommandData

    interface SecondStage {

        val mainSequenceId: Int
        val constrainedData: RemoraCommandData
    }

    @Serializable
    @SerialName("Initial")
    data class Initial(
        override val timestamp: Instant,
        override val followerSequenceId: Int,
        override val originalData: RemoraCommandData,
        val lastAttempt: Instant?,
    ) : RemoraCommand()

    @Serializable
    @SerialName("Rejected")
    data class Rejected(
        override val timestamp: Instant,
        override val followerSequenceId: Int,
        override val originalData: RemoraCommandData,
        val error: RemoraCommandError,
    ) : RemoraCommand()

    @Serializable
    @SerialName("Prepared")
    data class Prepared(
        override val timestamp: Instant,
        override val followerSequenceId: Int,
        override val originalData: RemoraCommandData,
        override val mainSequenceId: Int,
        override val constrainedData: RemoraCommandData,
        val validUntil: Instant,
        val lastAttempt: Instant?,
    ) : RemoraCommand(), SecondStage

    @Serializable
    @SerialName("Progressing")
    data class Progressing(
        override val timestamp: Instant,
        override val followerSequenceId: Int,
        override val originalData: RemoraCommandData,
        override val mainSequenceId: Int,
        override val constrainedData: RemoraCommandData,
        val progress: Progress,
    ) : RemoraCommand(), SecondStage

    @Serializable
    @SerialName("Final")
    data class Final(
        override val timestamp: Instant,
        override val followerSequenceId: Int,
        override val originalData: RemoraCommandData,
        override val mainSequenceId: Int,
        override val constrainedData: RemoraCommandData,
        val result: Result,
    ) : RemoraCommand(), SecondStage

    @Serializable
    @SerialName("Result")
    sealed class Result {

        @Serializable
        @SerialName("Success")
        data class Success(val finalData: RemoraCommandData) : Result()

        @Serializable
        @SerialName("Error")
        data class Error(val error: RemoraCommandError) : Result()
    }

    @Serializable
    @SerialName("Progress")
    sealed class Progress {
        @Serializable
        @SerialName("Connecting")
        data class Connecting(val startedAt: Instant) : Progress()
        @Serializable
        @SerialName("Enqueued")
        object Enqueued : Progress()
        @Serializable
        @SerialName("Percentage")
        data class Percentage(val percent: Int) : Progress()
    }
}