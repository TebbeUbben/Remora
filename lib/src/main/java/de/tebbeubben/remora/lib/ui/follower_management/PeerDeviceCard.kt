package de.tebbeubben.remora.lib.ui.follower_management

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import de.tebbeubben.remora.lib.R
import de.tebbeubben.remora.lib.model.PeerDevice
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
internal fun PeerDeviceCard(
    peerDevice: PeerDevice,
    viewModel: FollowerManagementViewModel,
    onStartPairing: (id: Long?) -> Unit
) {
    var openDeleteDialog by remember { mutableStateOf(false) }
    var openRenameDialog by remember { mutableStateOf(false) }

    if (openDeleteDialog) {
        DeleteDeviceDialog(
            onDismissRequest = { openDeleteDialog = false },
            onConfirm = {
                viewModel.deletePeerDevice(peerDevice.id)
                openDeleteDialog = false
            }
        )
    }

    if (openRenameDialog) {
        RenameDeviceDialog(
            initialDeviceName = (peerDevice as? PeerDevice.Paired)?.deviceName ?: "",
            onDismissRequest = { openRenameDialog = false },
            onConfirm = { newName ->
                viewModel.renameDevice(peerDevice.id, newName)
                openRenameDialog = false
            }
        )
    }

    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        ListItem(
            leadingContent = {
                Icon(
                    painter = painterResource(R.drawable.mobile_24px),
                    contentDescription = null
                )
            },
            headlineContent = {
                if (peerDevice is PeerDevice.Paired) {
                    Text(
                        peerDevice.deviceName ?: stringResource(R.string.remoraUnnamed_device),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                } else {
                    Text(
                        stringResource(R.string.remoraNew_follower_device),
                        fontStyle = FontStyle.Italic,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            },
            supportingContent = {
                if (peerDevice is PeerDevice.Paired) {
                    val pairedAt = peerDevice.pairedAt
                    Text(
                        stringResource(
                            R.string.remoraPaired_on, DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG).format(
                                LocalDateTime.ofInstant(Instant.ofEpochMilli(pairedAt), ZoneId.systemDefault())
                            )
                        )
                    )
                } else {
                    Text(
                        stringResource(R.string.remoraPairing_incomplete),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            trailingContent = {
                if (peerDevice is PeerDevice.Paired) {
                    IconButton(
                        onClick = {
                            openRenameDialog = true
                        }
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.edit_24px),
                            contentDescription = null
                        )
                    }
                }
            }
        )

        Text(
            "Device ID: ${peerDevice.id}",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(20.dp, 0.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp, 12.dp),
            horizontalArrangement = Arrangement.spacedBy(
                8.dp,
                Alignment.End
            ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = { openDeleteDialog = true }
            ) {
                Text(stringResource(R.string.remoraDelete))
            }
            if (peerDevice !is PeerDevice.Paired) {
                Button(
                    onClick = {
                        onStartPairing(peerDevice.id)
                    }
                ) {
                    Text(stringResource(R.string.remoraContinue_pairing))
                }
            }
        }
    }
}
