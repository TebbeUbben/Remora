package de.tebbeubben.remora.lib.ui.follower_management

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import de.tebbeubben.remora.lib.R

@Composable
internal fun RenameDeviceDialog(
    initialDeviceName: String,
    onDismissRequest: () -> Unit,
    onConfirm: (newName: String) -> Unit
) {
    var newDeviceName by remember { mutableStateOf(initialDeviceName) }

    AlertDialog(
        onDismissRequest = {
            onDismissRequest()
            newDeviceName = initialDeviceName // Reset the name when dialog is dismissed
        },
        title = { Text(stringResource(R.string.remoraRename_device)) },
        text = {
            OutlinedTextField(
                value = newDeviceName,
                onValueChange = { newDeviceName = it },
                label = { Text(stringResource(R.string.remoraNew_device_name)) },
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(newDeviceName)
                }
            ) {
                Text(stringResource(R.string.remoraRename))
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onDismissRequest()
                    newDeviceName = initialDeviceName
                }
            ) {
                Text(stringResource(R.string.remoraCancel))
            }
        }
    )
}
