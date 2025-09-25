package de.tebbeubben.remora.lib.ui.follower_management

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import de.tebbeubben.remora.lib.R

@Composable
internal fun DeleteDeviceDialog(
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.remoraDelete))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(R.string.remoraDismiss))
            }
        },
        title = { Text(stringResource(R.string.remoraDelete_device)) },
        text = { Text(stringResource(R.string.remoraThis_will_stop_the_device_from_communicating_with_this_app)) }
    )
}
