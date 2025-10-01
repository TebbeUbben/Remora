@file:UseSerializers(InstantComponentSerializer::class)

package de.tebbeubben.remora.lib.model.commands

import de.tebbeubben.remora.proto.commands.TreatmentCommand
import de.tebbeubben.remora.proto.commands.TreatmentCommandKt
import de.tebbeubben.remora.proto.commands.treatmentCommand
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.builtins.InstantComponentSerializer
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

@Serializable
@SerialName("CommandData")
sealed class RemoraCommandData {

    @Serializable
    @SerialName("Treatment")
    data class Treatment(
        val timestamp: Instant? = null,
        val bolusAmount: Float = 0f,

        val carbsAmount: Float = 0f,
        val carbsDuration: Duration = Duration.ZERO,
        val carbsOffset: Duration = Duration.ZERO,

        val temporaryTarget: TemporaryTarget? = null,
    ) : RemoraCommandData() {

        @Serializable
        @SerialName("TemporaryTarget")
        sealed class TemporaryTarget {

            @Serializable
            @SerialName("CancelTemporaryTarget")
            object Cancel : TemporaryTarget()

            @Serializable
            @SerialName("SetTemporaryTarget")
            data class Set(
                val ttType: TemporaryTargetType,
                val target: Float,
                val duration: Duration
            ) : TemporaryTarget()
        }

        @Serializable
        @SerialName("TemporaryTargetType")
        enum class TemporaryTargetType {
            CUSTOM,
            EATING_SOON,
            ACTIVITY,
            HYPO
        }
    }
}

internal fun RemoraCommandData.Treatment.toProtobuf() = treatmentCommand {
    this@toProtobuf.timestamp?.let { timestamp = it.epochSeconds }

    bolusAmount = this@toProtobuf.bolusAmount

    carbsAmount = this@toProtobuf.carbsAmount
    carbsDuration = this@toProtobuf.carbsDuration.inWholeMinutes.toInt()
    carbsOffset = this@toProtobuf.carbsOffset.inWholeMinutes.toInt()

    when (val tt = this@toProtobuf.temporaryTarget) {
        RemoraCommandData.Treatment.TemporaryTarget.Cancel -> {
            cancelTemporaryTarget = true
        }
        is RemoraCommandData.Treatment.TemporaryTarget.Set -> {
            setTemporaryTarget = TreatmentCommandKt.temporaryTarget {
                type = when (tt.ttType) {
                    RemoraCommandData.Treatment.TemporaryTargetType.CUSTOM      -> TreatmentCommand.TemporaryTargetType.TYPE_CUSTOM
                    RemoraCommandData.Treatment.TemporaryTargetType.EATING_SOON -> TreatmentCommand.TemporaryTargetType.TYPE_EATING_SOON
                    RemoraCommandData.Treatment.TemporaryTargetType.ACTIVITY    -> TreatmentCommand.TemporaryTargetType.TYPE_ACTIVITY
                    RemoraCommandData.Treatment.TemporaryTargetType.HYPO        -> TreatmentCommand.TemporaryTargetType.TYPE_HYPO
                }
                target = tt.target
                duration = tt.duration.inWholeMinutes.toInt()
            }
        }
        null                                               -> Unit
    }
}

internal fun TreatmentCommand.toModel() = RemoraCommandData.Treatment(
    timestamp = if (hasTimestamp()) Instant.fromEpochSeconds(timestamp) else null,
    bolusAmount = bolusAmount,
    carbsAmount = carbsAmount,
    carbsDuration = carbsDuration.minutes,
    carbsOffset = carbsOffset.minutes,
    temporaryTarget = when(temporaryTargetCase) {
        TreatmentCommand.TemporaryTargetCase.SET_TEMPORARY_TARGET    -> {
            val tt = setTemporaryTarget
            RemoraCommandData.Treatment.TemporaryTarget.Set(
                ttType = when (tt.type) {
                    TreatmentCommand.TemporaryTargetType.TYPE_CUSTOM      -> RemoraCommandData.Treatment.TemporaryTargetType.CUSTOM
                    TreatmentCommand.TemporaryTargetType.TYPE_EATING_SOON -> RemoraCommandData.Treatment.TemporaryTargetType.EATING_SOON
                    TreatmentCommand.TemporaryTargetType.TYPE_ACTIVITY    -> RemoraCommandData.Treatment.TemporaryTargetType.ACTIVITY
                    TreatmentCommand.TemporaryTargetType.TYPE_HYPO        -> RemoraCommandData.Treatment.TemporaryTargetType.HYPO
                    else                                                  -> throw IllegalArgumentException("Unknown temporary target type: ${tt.type}")
                },
                target = tt.target,
                duration = tt.duration.minutes
            )
        }
        TreatmentCommand.TemporaryTargetCase.CANCEL_TEMPORARY_TARGET -> {
            if (cancelTemporaryTarget) RemoraCommandData.Treatment.TemporaryTarget.Cancel else null
        }
        TreatmentCommand.TemporaryTargetCase.TEMPORARYTARGET_NOT_SET -> null
    }
)