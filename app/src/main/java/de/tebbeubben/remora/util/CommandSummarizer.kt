package de.tebbeubben.remora.util

import android.content.Context
import android.text.Html
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.fromHtml
import dagger.Reusable
import dagger.hilt.android.qualifiers.ApplicationContext
import de.tebbeubben.remora.R
import de.tebbeubben.remora.lib.model.commands.RemoraCommandData
import de.tebbeubben.remora.lib.model.commands.RemoraCommandError
import javax.inject.Inject


val LocalCommandSummarizer = staticCompositionLocalOf<CommandSummarizer> {
    error("No CommandSummarizer provided")
}

@Reusable
class CommandSummarizer @Inject constructor(
    @param:ApplicationContext
    private val context: Context
) {

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
            context.getString(R.string.bolus_summary_changed, bolusAmount, previousBolusAmount)
        } else {
            context.getString(R.string.bolus_summary, bolusAmount)
        }
        val eatingSoonTTText = if (previous != null && previous.startEatingSoonTT && !data.startEatingSoonTT) {
            context.getString(R.string.summary_start_eating_soon_changed)
        } else if (data.startEatingSoonTT) {
            context.getString(R.string.summary_start_eating_soon)
        } else ""

        return bolusAmountText + eatingSoonTTText
    }

    fun translateError(error: RemoraCommandError) = when (error) {
        RemoraCommandError.UNKNOWN             -> context.getString(R.string.an_unknown_error_occurred)
        RemoraCommandError.BOLUS_IN_PROGRESS   -> context.getString(R.string.another_bolus_is_already_in_progress)
        RemoraCommandError.PUMP_SUSPENDED      -> context.getString(R.string.bolus_delivery_is_not_possible_because_the_pump_is_currently_suspended)
        RemoraCommandError.BG_MISMATCH         -> context.getString(R.string.the_bg_value_on_this_device_does_not_match_the_value_on_the_main_phone)
        RemoraCommandError.IOB_MISMATCH        -> context.getString(R.string.the_iob_value_on_this_device_does_not_match_the_value_on_the_main_phone)
        RemoraCommandError.COB_MISMATCH        -> context.getString(R.string.the_cob_value_on_this_device_does_not_match_the_value_on_the_main_phone)
        RemoraCommandError.LAST_BOLUS_MISMATCH -> context.getString(R.string.the_last_bolus_recorded_on_this_device_does_not_match_the_record_on_the_main_phone)
        RemoraCommandError.PUMP_TIMEOUT        -> context.getString(R.string.the_pump_did_not_respond_in_time_please_try_again)
        RemoraCommandError.WRONG_SEQUENCE_ID   -> context.getString(R.string.wrong_sequence_number_another_follower_device_may_be_issuing_commands_at_the_same_time)
        RemoraCommandError.EXPIRED             -> context.getString(R.string.this_command_has_expired_please_start_again)
        RemoraCommandError.ACTIVE_COMMAND      -> context.getString(R.string.another_command_is_already_being_executed_another_follower_device_may_be_issuing_commands_at_the_same_time)
        RemoraCommandError.INVALID_VALUE       -> context.getString(R.string.some_of_the_entered_data_was_invalid_please_check_your_input_and_try_again)
    }
}