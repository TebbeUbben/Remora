package de.tebbeubben.remora.lib.model.commands

import de.tebbeubben.remora.proto.commands.CommandError
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("CommandError")
enum class RemoraCommandError {
    UNKNOWN,
    BOLUS_IN_PROGRESS,
    PUMP_SUSPENDED,
    BG_MISMATCH,
    IOB_MISMATCH,
    COB_MISMATCH,
    LAST_BOLUS_MISMATCH,
    PUMP_TIMEOUT,
    WRONG_SEQUENCE_ID,
    EXPIRED,
    ACTIVE_COMMAND
}

internal fun CommandError.toModel() = when (this) {
    CommandError.ERROR_UNKNOWN             -> RemoraCommandError.UNKNOWN
    CommandError.ERROR_PUMP_SUSPENDED      -> RemoraCommandError.PUMP_SUSPENDED
    CommandError.ERROR_BOLUS_IN_PROGRESS   -> RemoraCommandError.BOLUS_IN_PROGRESS
    CommandError.ERROR_BG_MISMATCH         -> RemoraCommandError.BG_MISMATCH
    CommandError.ERROR_IOB_MISMATCH        -> RemoraCommandError.IOB_MISMATCH
    CommandError.ERROR_COB_MISMATCH        -> RemoraCommandError.COB_MISMATCH
    CommandError.ERROR_LAST_BOLUS_MISMATCH -> RemoraCommandError.LAST_BOLUS_MISMATCH
    CommandError.ERROR_PUMP_TIMEOUT        -> RemoraCommandError.PUMP_TIMEOUT
    CommandError.ERROR_WRONG_SEQUENCE_ID   -> RemoraCommandError.WRONG_SEQUENCE_ID
    CommandError.ERROR_EXPIRED             -> RemoraCommandError.EXPIRED
    CommandError.ERROR_ACTIVE_COMMAND      -> RemoraCommandError.ACTIVE_COMMAND
    else                                   -> RemoraCommandError.UNKNOWN
}

internal fun RemoraCommandError.toProtobuf() = when (this) {
    RemoraCommandError.UNKNOWN             -> CommandError.ERROR_UNKNOWN
    RemoraCommandError.BOLUS_IN_PROGRESS   -> CommandError.ERROR_BOLUS_IN_PROGRESS
    RemoraCommandError.PUMP_SUSPENDED      -> CommandError.ERROR_PUMP_SUSPENDED
    RemoraCommandError.BG_MISMATCH         -> CommandError.ERROR_BG_MISMATCH
    RemoraCommandError.IOB_MISMATCH        -> CommandError.ERROR_IOB_MISMATCH
    RemoraCommandError.COB_MISMATCH        -> CommandError.ERROR_COB_MISMATCH
    RemoraCommandError.LAST_BOLUS_MISMATCH -> CommandError.ERROR_LAST_BOLUS_MISMATCH
    RemoraCommandError.PUMP_TIMEOUT        -> CommandError.ERROR_PUMP_TIMEOUT
    RemoraCommandError.WRONG_SEQUENCE_ID   -> CommandError.ERROR_WRONG_SEQUENCE_ID
    RemoraCommandError.EXPIRED             -> CommandError.ERROR_EXPIRED
    RemoraCommandError.ACTIVE_COMMAND      -> CommandError.ERROR_ACTIVE_COMMAND
}