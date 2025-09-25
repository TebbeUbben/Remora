package de.tebbeubben.remora.lib.ui.follower_management

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import de.tebbeubben.remora.lib.R

@Composable
internal fun ResetRemoraDialog(
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(
                onClick = onConfirm
            ) {
                Text(stringResource(R.string.remoraReset))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismissRequest
            ) {
                Text(stringResource(R.string.remoraDismiss))
            }
        },
        title = { Text(stringResource(R.string.remoraReset_remora)) },
        text = { Text(stringResource(R.string.remoraThis_will_immediately_unlink_all_followers_remora_must_be_configured_again)) }
    )
}
