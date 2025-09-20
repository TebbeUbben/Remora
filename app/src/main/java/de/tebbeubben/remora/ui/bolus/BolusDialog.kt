package de.tebbeubben.remora.ui.bolus

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable
fun BolusDialog(
    onDismiss: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            ),
            shape = MaterialTheme.shapes.extraLarge
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Deliver Bolus",
                    style = MaterialTheme.typography.headlineSmall
                )

                Spacer(Modifier.height(16.dp))

                var step by remember { mutableStateOf(1) }

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalStepperItem(
                        stepNumber = "1",
                        text = "Select Amount",
                        isActive = step == 1,
                    )

                    Spacer(Modifier.width(8.dp))

                    HorizontalStepperItem(
                        stepNumber = "2",
                        text = "Apply Limits",
                        isActive = step == 2,
                    )

                    Spacer(Modifier.width(8.dp))

                    HorizontalStepperItem(
                        stepNumber = "3",
                        text = "Confirm",
                        isActive = step == 3,
                    )

                    Spacer(Modifier.width(8.dp))

                    HorizontalStepperItem(
                        stepNumber = "4",
                        text = "Deliver",
                        isActive = step == 4,
                    )
                }

                Spacer(Modifier.height(16.dp))

                val focusRequester = remember { FocusRequester() }

                LaunchedEffect(Unit) {
                    focusRequester.requestFocus()
                }

                InsulinAmountField(
                    modifier = Modifier.width(200.dp).focusRequester(focusRequester),
                    callback = {
                        Log.d("BolusDialog", it.toString())
                    },
                )

                Spacer(Modifier.height(16.dp))

                Box(Modifier.fillMaxWidth(), Alignment.Center) {
                    var checked by remember { mutableStateOf(false) }
                    Row(
                        Modifier
                            .clip(MaterialTheme.shapes.small)
                            .toggleable(
                                value = checked,
                                onValueChange = { checked = !checked },
                                role = Role.Checkbox,
                            )
                            .padding(16.dp)
                    ) {
                        Checkbox(
                            checked = checked,
                            onCheckedChange = null,
                        )
                        Text(
                            text = "Start Eating Soon TT",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 16.dp),
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                ) {
                    TextButton(
                        onClick = {
                            onDismiss()
                        }
                    ) {
                        Text("Cancel")
                    }
                    TextButton(
                        onClick = {
                        }
                    ) {
                        Text("Confirm")
                    }
                }
            }
        }
    }
}