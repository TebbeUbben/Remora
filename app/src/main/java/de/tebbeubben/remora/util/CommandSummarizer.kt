package de.tebbeubben.remora.util

import android.text.Html
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.fromHtml
import dagger.Reusable
import de.tebbeubben.remora.lib.model.commands.RemoraCommandData
import de.tebbeubben.remora.lib.model.commands.RemoraCommandError
import javax.inject.Inject


val LocalCommandSummarizer = staticCompositionLocalOf<CommandSummarizer> {
    error("No CommandSummarizer provided")
}

@Reusable
class CommandSummarizer @Inject constructor() {

    fun spanned(data: RemoraCommandData, previous: RemoraCommandData?) =
        Html.fromHtml(summarizeData(data, previous), Html.FROM_HTML_MODE_LEGACY or Html.FROM_HTML_SEPARATOR_LINE_BREAK_PARAGRAPH)

    fun annotatedString(data: RemoraCommandData, previous: RemoraCommandData?) =
        AnnotatedString.fromHtml(summarizeData(data, previous))

    private fun summarizeData(data: RemoraCommandData, previous: RemoraCommandData?) = when (data) {
        is RemoraCommandData.Bolus -> bolusSummary(data, previous as RemoraCommandData.Bolus?)
    }

    private fun bolusSummary(
        data: RemoraCommandData.Bolus,
        previous: RemoraCommandData.Bolus?,
    ): String {
        val bolusAmount = data.bolusAmount.formatInsulin()
        val previousBolusAmount = previous?.bolusAmount?.formatInsulin()
        val bolusAmountText = if (previousBolusAmount != null && previousBolusAmount != bolusAmount) {
            "<p>Bolus: <b>$bolusAmount U</b> <s>$previousBolusAmount U</s></p>"
        } else {
            "<p>Bolus: <b>$bolusAmount U</b></p>"
        }
        val eatingSoonTTText = if (previous != null && previous.startEatingSoonTT && !data.startEatingSoonTT) {
            "<p><s>Start Eating Soon TT</s></p>"
        } else if (data.startEatingSoonTT) {
            "<p>Start Eating Soon TT</p>"
        } else ""

        return bolusAmountText + eatingSoonTTText
    }

    fun translateError(error: RemoraCommandError) = when (error) {
        RemoraCommandError.UNKNOWN             -> "An unknown error occurred."
        RemoraCommandError.BOLUS_IN_PROGRESS   -> "Another bolus is already in progress."
        RemoraCommandError.PUMP_SUSPENDED      -> "Bolus delivery is not possible because the pump is currently suspended."
        RemoraCommandError.BG_MISMATCH         -> "The BG value on this device does not match the value on the main phone."
        RemoraCommandError.IOB_MISMATCH        -> "The IOB value on this device does not match the value on the main phone."
        RemoraCommandError.COB_MISMATCH        -> "The COB value on this device does not match the value on the main phone."
        RemoraCommandError.LAST_BOLUS_MISMATCH -> "The last bolus recorded on this device does not match the record on the main phone."
        RemoraCommandError.PUMP_TIMEOUT        -> "The pump did not respond in time. Please try again."
        RemoraCommandError.WRONG_SEQUENCE_ID   -> "Wrong sequence number. Another follower device may be issuing commands at the same time."
        RemoraCommandError.EXPIRED             -> "This command has expired. Please start again."
        RemoraCommandError.ACTIVE_COMMAND      -> "Another command is already being executed. Another follower device may be issuing commands at the same time."
        RemoraCommandError.INVALID_VALUE       -> "Some of the entered data was invalid. Please check your input and try again."
    }
}