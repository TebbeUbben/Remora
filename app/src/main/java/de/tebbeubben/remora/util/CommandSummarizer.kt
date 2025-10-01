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
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toLocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import javax.inject.Inject
import kotlin.math.roundToInt
import kotlin.time.Duration

val LocalCommandSummarizer = staticCompositionLocalOf<CommandSummarizer> {
    error("No CommandSummarizer provided")
}

@Reusable
class CommandSummarizer @Inject constructor(
    @param:ApplicationContext
    private val context: Context,
) {

    fun spanned(usesMgdl: Boolean, data: RemoraCommandData, previous: RemoraCommandData?) =
        Html.fromHtml(summarizeData(usesMgdl, data, previous), Html.FROM_HTML_MODE_LEGACY)

    fun annotatedString(usesMgdl: Boolean, data: RemoraCommandData, previous: RemoraCommandData?) =
        AnnotatedString.fromHtml(summarizeData(usesMgdl, data, previous))

    private fun summarizeData(usesMgdl: Boolean, data: RemoraCommandData, previous: RemoraCommandData?) = when (data) {
        is RemoraCommandData.Treatment -> bolusSummary(usesMgdl, data, previous as RemoraCommandData.Treatment?)
    }

    private fun bolusSummary(
        usesMgdl: Boolean,
        data: RemoraCommandData.Treatment,
        previous: RemoraCommandData.Treatment?,
    ): String {
        val lines = mutableListOf<String>()

        if (data.bolusAmount > 0f || previous != null && previous.bolusAmount > 0f) {
            if (data.timestamp == null) {
                val bolusAmount = data.bolusAmount.formatInsulin()
                val previousBolusAmount = previous?.bolusAmount?.formatInsulin()
                lines += if (previousBolusAmount != null && previousBolusAmount != bolusAmount) {
                    context.getString(R.string.bolus_summary_changed, bolusAmount, previousBolusAmount)
                } else {
                    context.getString(R.string.bolus_summary, bolusAmount)
                }
            } else {
                val bolusAmount = data.bolusAmount.formatInsulin()
                val previousBolusAmount = previous?.bolusAmount?.formatInsulin()
                lines += if (previousBolusAmount != null && previousBolusAmount != bolusAmount) {
                    "Insulin: <b>$bolusAmount U</b> <s>$previousBolusAmount U</s>"
                } else {
                    "Insulin: <b>$bolusAmount U</b>"
                }
            }
        }

        if (data.carbsAmount > 0f || previous != null && previous.carbsAmount > 0f) {
            val carbsAmount = data.carbsAmount.roundToInt()
            val duration = if (data.carbsDuration == Duration.ZERO) null else data.carbsDuration.formatHoursAndMinutes()

            val formatted = if (duration == null) {
                context.getString(R.string.carbs_formatter, carbsAmount)
            } else {
                context.getString(R.string.carbs_formatter_duration, carbsAmount, duration)
            }

            val previousCarbs = previous?.carbsAmount?.roundToInt()
            val previousDuration = if (previous == null || previous.carbsDuration == Duration.ZERO) null else data.carbsDuration.formatHoursAndMinutes()

            val previousFormatted = when {
                previousCarbs == null    -> null
                previousDuration == null -> context.getString(R.string.carbs_formatter, previousCarbs)
                else                     -> context.getString(R.string.carbs_formatter_duration, previousCarbs, previousDuration)
            }

            lines += if (previousFormatted != null && previousFormatted != formatted) {
                context.getString(R.string.carbs_summary_changed, formatted, previousFormatted)
            } else {
                context.getString(R.string.carbs_summary, formatted)
            }
        }

        if (data.temporaryTarget != null || previous != null && previous.temporaryTarget != null) {
            val formatted = data.temporaryTarget?.format(usesMgdl)
            val previousFormatted = previous?.temporaryTarget?.format(usesMgdl)

            lines += if (previousFormatted != null && previousFormatted != formatted) {
                context.getString(R.string.temp_target_summary_changed, formatted, previousFormatted)
            } else {
                context.getString(R.string.temp_target_summary, formatted)
            }
        }

        val timezone = TimeZone.currentSystemDefault()
        if (data.timestamp != null) {
            val formatted = data.timestamp!!.toLocalDateTime(timezone).toJavaLocalDateTime()
                .format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.LONG, FormatStyle.SHORT))
            val previousFormatted = previous?.timestamp?.toLocalDateTime(timezone)?.toJavaLocalDateTime()
                ?.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.LONG, FormatStyle.SHORT))
            lines += if (previousFormatted != null && previousFormatted != formatted) {
                context.getString(R.string.time_summary, formatted, previousFormatted)
            } else {
                context.getString(R.string.time_summary_changed, formatted)
            }
        }

        return lines.joinToString("<br>")
    }

    private fun RemoraCommandData.Treatment.TemporaryTarget.format(usesMgdl: Boolean): String {
        return when (this) {
            is RemoraCommandData.Treatment.TemporaryTarget.Set    -> {
                val target = target.formatBG(usesMgdl) + if (usesMgdl) " mg/dL" else " mmol/L"
                val duration = duration.formatHoursAndMinutes()
                context.getString(R.string.temp_target_formatter, target, duration)
            }

            is RemoraCommandData.Treatment.TemporaryTarget.Cancel -> context.getString(R.string.cancel)
        }
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