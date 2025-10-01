package de.tebbeubben.remora.ui.commands

import android.view.textclassifier.TextSelection
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign

private val ALLOWED_CHARACTERS = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9')

@Composable
fun CarbsAmountField(
    modifier: Modifier = Modifier,
    colorVariant: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    initialAmount: Int? = null,
    isError: Boolean = false,
    amountState: MutableState<Int>,
    label: String,
    textStyle: TextStyle = MaterialTheme.typography.headlineLarge.copy(textAlign = TextAlign.Center),
    imeAction: ImeAction = ImeAction.Default,
) {
    val state = if (initialAmount == null) {
        rememberTextFieldState("\u200B", TextRange(0))
    } else {
        rememberTextFieldState(initialAmount.toString())
    }

    LaunchedEffect(state.text) {
        amountState.value = if (state.text == "\u200B") 0 else state.text.toString().toInt()
    }

    OutlinedTextField(
        modifier = modifier,
        state = state,
        lineLimits = TextFieldLineLimits.SingleLine,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = imeAction
        ),
        textStyle = textStyle,
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

            while (length > 0 && charAt(0) == '0') {
                // Remove leading zeros
                replace(0, 1, "")
            }

            if (length > 3) {
                replace(3, length, "")
            }

            if (length == 0) {
                // "Zero width white space" to control cursor position after output transformation
                insert(0, "\u200B")
                selection = TextRange(0)
            }
        },
        outputTransformation = OutputTransformation {
            if (length == 0) return@OutputTransformation
            if (charAt(0) == '\u200B') {
                replace(0, 1, "0")
                addStyle(SpanStyle(color = colorVariant), 0, 1)
            }
            append(" g")
        }
    )
}