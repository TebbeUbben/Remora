package de.tebbeubben.remora.ui.commands

import android.icu.number.NumberFormatter
import android.icu.number.Precision
import android.icu.text.DecimalFormatSymbols
import android.icu.util.ULocale
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.OutputTransformation
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.insert
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign

private val ALLOWED_CHARACTERS = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '.', ',')

@Composable
fun InsulinAmountField(
    modifier: Modifier = Modifier,
    colorVariant: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    initialAmount: Float? = null,
    isError: Boolean = false,
    amountState: MutableState<Float>,
    label: String
) {

    val initialText = initialAmount?.let {
        NumberFormatter.withLocale(ULocale.getDefault())
            .precision(Precision.fixedFraction(2))
            .format(initialAmount)
            .toString()
    }

    val state = rememberTextFieldState(initialText ?: "\u200B", TextRange(initialText?.length ?: 1))

    LaunchedEffect(state.text) {
        var text = state.text.toString().replace(',', '.').replace("\u200B", "")
        if (text.indexOfFirst { it == '.' } == -1) {
            text = when (text.length) {
                0 -> "0.00"
                1 -> "0.0$text"
                2 -> "0.$text"
                else -> text.dropLast(2) + "." + text.substring(text.length - 2)
            }
        } else if (text == ".") {
            text = "0.0"
        }
        val amount = text.toFloat()
        amountState.value = amount
    }

    val largeTextStyle = MaterialTheme.typography.headlineLarge.toSpanStyle()
    val smallTextStyle = MaterialTheme.typography.headlineSmall.toSpanStyle()

    OutlinedTextField(
        modifier = modifier,
        state = state,
        lineLimits = TextFieldLineLimits.SingleLine,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Done
        ),
        textStyle = MaterialTheme.typography.headlineLarge.copy(textAlign = TextAlign.Center),
        label = {
            Text(label)
        },
        isError = isError,
        inputTransformation = {
            // Remove unallowed characters
            var pos = 0
            while (pos < length) {
                if (charAt(pos) !in ALLOWED_CHARACTERS) {
                    replace(pos, pos + 1, "")
                } else {
                    pos++
                }
            }
            val decimalDelimiter = DecimalFormatSymbols.getInstance().decimalSeparator
            var decimalPos = asCharSequence().indexOfFirst { it == '.' || it == ',' }
            if (decimalPos != -1) {
                // Make sure the decimal delimiter is correct for the current locale
                if (charAt(decimalPos) != decimalDelimiter) {
                    replace(decimalPos, decimalPos + 1, decimalDelimiter.toString())
                }

                // Remove any extra decimal delimiters
                var pos = decimalPos + 1
                while (pos < length) {
                    if (charAt(pos) == '.' || charAt(pos) == ',') {
                        replace(pos, pos + 1, "")
                    } else {
                        pos++
                    }
                }

                if (length - decimalPos - 1 > 2) {
                    // Fractional part is overflowing -> remove extra digits
                    replace(decimalPos + 3, length, "")
                }

                if (decimalPos > 2) {
                    // Integer part is overflowing, remove any extra digits
                    replace(0, decimalPos - 2, "")
                    decimalPos = 2
                }

                if (decimalPos == 2 && charAt(0) == '0') {
                    // Remove leading zero from integer part
                    replace(0, decimalPos--, "0")
                }
            } else {
                if (length > 4) {
                    replace(4, length, "")
                }

                while (length > 0 && charAt(0) == '0') {
                    // Remove leading zeros
                    replace(0, 1, "")
                }
            }

            if (length == 0) {
                // "Zero width white space" to control cursor position after output transformation
                insert(0, "\u200B")
                selection = TextRange(1, 1)
            }
        },
        outputTransformation = OutputTransformation {
            if (length == 0) return@OutputTransformation
            val decimalDelimiter = DecimalFormatSymbols.getInstance().decimalSeparatorString
            if (charAt(0) == '\u200B') {
                replace(0, 1, "0${decimalDelimiter}00")
                addStyle(largeTextStyle.copy(color = colorVariant), 0, 1)
                addStyle(smallTextStyle.copy(color = colorVariant), 2, 4)
            } else {
                var decimalPos = asCharSequence().indexOf(decimalDelimiter)
                if (decimalPos == -1) {
                    when (length) {
                        1 -> {
                            insert(0, "0${decimalDelimiter}0")
                            addStyle(largeTextStyle.copy(color = colorVariant), 0, 1)
                            addStyle(smallTextStyle.copy(color = colorVariant), 2, 3)
                            addStyle(smallTextStyle, 3, 4)
                        }

                        2 -> {
                            insert(0, "0${decimalDelimiter}")
                            addStyle(largeTextStyle.copy(color = colorVariant), 0, 1)
                            addStyle(smallTextStyle, 2, 4)
                        }

                        3 -> {
                            insert(1, decimalDelimiter)
                            addStyle(smallTextStyle, 2, 4)
                        }

                        4 -> {
                            insert(2, decimalDelimiter)
                            addStyle(smallTextStyle, 3, 5)
                        }
                    }
                } else {
                    if (decimalPos == 0) {
                        insert(decimalPos++, "0")
                        addStyle(largeTextStyle.copy(color = colorVariant), 0, 1)
                        decimalPos
                    }
                    when (length - decimalPos - 1) {
                        0 -> {
                            insert(decimalPos + 1, "00")
                            addStyle(smallTextStyle.copy(color = colorVariant), decimalPos + 1, decimalPos + 3)
                        }

                        1 -> {
                            insert(decimalPos + 2, "0")
                            addStyle(smallTextStyle, decimalPos + 1, decimalPos + 2)
                            addStyle(smallTextStyle.copy(color = colorVariant), decimalPos + 2, decimalPos + 3)
                        }

                        2 -> {
                            addStyle(smallTextStyle, decimalPos + 1, decimalPos + 3)
                        }
                    }
                }
            }

            append(" U")
        }
    )
}