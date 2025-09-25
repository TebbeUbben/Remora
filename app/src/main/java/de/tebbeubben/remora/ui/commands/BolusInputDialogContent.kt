package de.tebbeubben.remora.ui.commands

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun ColumnScope.BolusInputDialogContent(
    onCancel: () -> Unit,
    onValidate: (bolusAmount: Float, startEatingSoonTT: Boolean) -> Unit
) {
    Text(
        text = "Deliver Bolus",
        style = MaterialTheme.typography.headlineSmall,
        textAlign = TextAlign.Center
    )
    Spacer(Modifier.height(16.dp))

    val focusRequester = remember { FocusRequester() }
    var bolusErrorMessage by remember { mutableStateOf<String?>(null) }
    val bolusAmount = rememberSaveable { mutableStateOf(0f) }
    var startEatingSoonTT by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(bolusAmount) {
        if (bolusErrorMessage != null && bolusAmount.value != 0f) {
            bolusErrorMessage = null
        }
    }


    InsulinAmountField(
        modifier = Modifier
            .width(150.dp)
            .focusRequester(focusRequester),
        amountState = bolusAmount,
        isError = bolusErrorMessage != null,
        label = "Amount"
    )

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    AnimatedVisibility(bolusErrorMessage != null) {
        Text(
            modifier = Modifier.padding(top = 8.dp),
            text = bolusErrorMessage ?: "",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center
        )
    }

    Spacer(Modifier.height(8.dp))

    Row(
        Modifier
            .clip(MaterialTheme.shapes.small)
            .toggleable(
                value = startEatingSoonTT,
                onValueChange = { startEatingSoonTT = !startEatingSoonTT },
                role = Role.Checkbox,
            )
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(
            checked = startEatingSoonTT,
            onCheckedChange = null,
        )
        Text(
            text = "Start Eating Soon TT",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(start = 16.dp),
        )
    }

    Spacer(Modifier.height(8.dp))

    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
    ) {
        TextButton(onCancel) {
            Text("Cancel")
        }
        TextButton(
            onClick = {
                if (bolusAmount.value == 0f) {
                    bolusErrorMessage = "Please enter a bolus amount."
                } else {
                    onValidate(bolusAmount.value, startEatingSoonTT)
                }
            }
        ) {
            Text("Validate")
        }
    }
}