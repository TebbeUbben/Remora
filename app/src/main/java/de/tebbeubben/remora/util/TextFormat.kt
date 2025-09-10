package de.tebbeubben.remora.util

import android.icu.number.NumberFormatter
import android.icu.number.Precision
import android.icu.text.RelativeDateTimeFormatter
import android.icu.util.ULocale
import java.util.Locale
import kotlin.math.roundToInt
import kotlin.time.Duration

fun Float.formatBG(useMgdl: Boolean) = if (useMgdl) {
    this.roundToInt().toString()
} else {
    NumberFormatter.withLocale(ULocale.getDefault())
        .precision(Precision.fixedFraction(1))
        .format(this / 18.018f)
        .toString()
}

fun Duration.toRelativeString(locale: Locale = Locale.getDefault()): String {
    val fmt = RelativeDateTimeFormatter.getInstance(locale)

    val totalMinutes = this.inWholeMinutes
    if (totalMinutes == 0L) {
        return fmt.format(RelativeDateTimeFormatter.Direction.PLAIN, RelativeDateTimeFormatter.AbsoluteUnit.NOW)
    }

    val absMinutes = kotlin.math.abs(totalMinutes)

    val (amount, unit) = when {
        absMinutes >= 60 * 24 * 365 -> absMinutes / (60 * 24 * 365) to RelativeDateTimeFormatter.RelativeUnit.YEARS
        absMinutes >= 60 * 24 * 30  -> absMinutes / (60 * 24 * 30) to RelativeDateTimeFormatter.RelativeUnit.MONTHS
        absMinutes >= 60 * 24 * 7   -> absMinutes / (60 * 24 * 7) to RelativeDateTimeFormatter.RelativeUnit.WEEKS
        absMinutes >= 60 * 24       -> absMinutes / (60 * 24) to RelativeDateTimeFormatter.RelativeUnit.DAYS
        absMinutes >= 60            -> absMinutes / 60 to RelativeDateTimeFormatter.RelativeUnit.HOURS
        else                        -> absMinutes to RelativeDateTimeFormatter.RelativeUnit.MINUTES
    }

    val direction = if (totalMinutes > 0) {
        RelativeDateTimeFormatter.Direction.NEXT
    } else {
        RelativeDateTimeFormatter.Direction.LAST
    }

    return fmt.format(amount.toDouble(), direction, unit)
}

fun Duration.toMinimalLocalizedString(): String {
    var days = 0L
    var hours = 0
    var minutes = 0

    this.absoluteValue.toComponents { d, h, m, s, _ ->
        days = d
        hours = h
        minutes = m
    }

    val sb = StringBuilder()
    if (days != 0L) sb.append(days).append('d')
    if (hours != 0 || sb.isNotEmpty()) sb.append(hours).append('h')
    sb.append(minutes).append('m')
    return if (isNegative()) "-${sb}" else sb.toString()
}

fun Duration.formatDaysAndHours(): String {
    var days = 0L
    var hours = 0

    this.absoluteValue.toComponents { d, h, m, s, _ ->
        days = d
        hours = h
    }

    val sb = StringBuilder()
    sb.append(days).append('d')
    sb.append(hours).append('h')
    return if (isNegative()) "-${sb}" else sb.toString()
}

fun Float.formatInsulin() =
    NumberFormatter.withLocale(ULocale.getDefault())
        .precision(Precision.fixedFraction(2))
        .format(this)
        .toString()

fun Float.formatCarbs() = this.roundToInt().toString()