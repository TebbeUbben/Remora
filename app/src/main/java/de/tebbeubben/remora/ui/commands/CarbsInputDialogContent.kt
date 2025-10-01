package de.tebbeubben.remora.ui.commands

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.tebbeubben.remora.R
import de.tebbeubben.remora.ui.util.rememberDuration
import de.tebbeubben.remora.ui.util.rememberInstant
import kotlin.time.Duration
import kotlin.time.Instant

@Composable
fun ColumnScope.CarbsInputDialogContent(
    onCancel: () -> Unit,
    onValidate: (carbsAmount: Int, duration: Duration, tempTarget: Int, timestamp: Instant?) -> Unit,
) {
    Text(
        text = stringResource(R.string.enter_carbs),
        style = MaterialTheme.typography.headlineSmall,
        textAlign = TextAlign.Center
    )
    Spacer(Modifier.height(16.dp))

    val context = LocalContext.current
    val focusRequester = remember { FocusRequester() }
    var carbsErrorMessage by remember { mutableStateOf<String?>(null) }
    val carbsAmount = rememberSaveable { mutableStateOf(0) }
    val duration = rememberDuration(Duration.ZERO)
    var tempTarget by remember { mutableStateOf(0) }
    val timestamp = rememberInstant(null)

    LaunchedEffect(carbsAmount.value) {
        if (carbsErrorMessage != null && carbsAmount.value != 0) {
            carbsErrorMessage = null
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
    ) {
        CarbsAmountField(
            modifier = Modifier
                .weight(1f)
                .focusRequester(focusRequester),
            amountState = carbsAmount,
            isError = carbsErrorMessage != null,
            label = stringResource(R.string.amount),
            imeAction = ImeAction.Next
        )

        DurationField(
            modifier = Modifier
                .weight(1.3f),
            durationState = duration,
            label = stringResource(R.string.duration),
            imeAction = ImeAction.Done
        )
    }

    AnimatedVisibility(carbsErrorMessage != null) {
        Text(
            modifier = Modifier.padding(top = 16.dp),
            text = carbsErrorMessage ?: "",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center
        )
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Spacer(Modifier.height(16.dp))

    val options: List<String> = listOf(
        stringResource(R.string.no_temporary_target),
        stringResource(R.string.activity),
        stringResource(R.string.eating_soon),
        stringResource(R.string.hypo)
    )
    
    var expanded by remember { mutableStateOf(false) }
    val textFieldState = rememberTextFieldState(options[0])

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
            state = textFieldState,
            readOnly = true,
            lineLimits = TextFieldLineLimits.SingleLine,
            label = { Text(stringResource(R.string.temporary_target)) },
            trailingIcon = {
                Icon(
                    modifier = Modifier.rotate(if (expanded) 180f else 0f),
                    painter = painterResource(R.drawable.arrow_drop_down_24px),
                    contentDescription = null
                )
            },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEachIndexed { index, option ->
                DropdownMenuItem(
                    text = { Text(option, style = MaterialTheme.typography.bodyLarge) },
                    onClick = {
                        tempTarget = index
                        textFieldState.setTextAndPlaceCursorAtEnd(option)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                )
            }
        }
    }

    Spacer(Modifier.height(8.dp))

    DateTimePicker(
        selection = timestamp
    )

    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
    ) {
        TextButton(onCancel) {
            Text(stringResource(R.string.cancel))
        }
        TextButton(
            onClick = {
                if (carbsAmount.value == 0) {
                    carbsErrorMessage = context.getString(R.string.please_enter_a_carbs_amount)
                } else {
                    onValidate(carbsAmount.value, duration.value, tempTarget, timestamp.value)
                }
            }
        ) {
            Text(stringResource(R.string.validate))
        }
    }
}
