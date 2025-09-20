package de.tebbeubben.remora.lib.model.commands

import de.tebbeubben.remora.proto.commands.BolusCommand
import de.tebbeubben.remora.proto.commands.bolusCommand
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("CommandData")
sealed class RemoraCommandData {

    @Serializable
    @SerialName("Bolus")
    data class Bolus(
        val bolusAmount: Float,
        val startEatingSoonTT: Boolean
    ) : RemoraCommandData()
}

internal fun RemoraCommandData.Bolus.toProtobuf() = bolusCommand {
    bolusAmount = this@toProtobuf.bolusAmount
    startEatingSoonTt = this@toProtobuf.startEatingSoonTT
}

internal fun BolusCommand.toModel() = RemoraCommandData.Bolus(
    bolusAmount = this.bolusAmount,
    startEatingSoonTT = this.startEatingSoonTt,
)