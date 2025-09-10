package de.tebbeubben.remora.lib.ui.follower_management

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import de.tebbeubben.remora.lib.R

@Composable
internal fun FcmConfigurationCard(
    fcmProjectId: String?,
    viewModel: FollowerManagementViewModel,
    startConfiguration: () -> Unit
) {
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        ListItem(
            leadingContent = {
                Icon(
                    painter = painterResource(R.drawable.cloud_24px),
                    contentDescription = null
                )
            },
            headlineContent = {
                Text(
                    "Firebase Cloud Messaging",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            supportingContent = {
                if (fcmProjectId != null) {
                    Text("Configured")
                } else {
                    Text(
                        "Not configured",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            trailingContent = {
                if (fcmProjectId != null) {
                    Icon(
                        painter = painterResource(R.drawable.check_circle_24px),
                        tint = MaterialTheme.colorScheme.primary,
                        contentDescription = null
                    )
                }
            }
        )

        if (fcmProjectId != null) {
            Text(
                "Project ID: $fcmProjectId",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(20.dp, 0.dp)
            )

            Text(
                "Remora is successfully configured to be used with Firebase Cloud Messaging. " +
                        "You can reset Remora to start over again. This will immediately unlink all followers " +
                        "and pairings must be established again.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(
                    start = 20.dp,
                    end = 20.dp,
                    top = 8.dp
                )
            )
        } else {
            Text(
                "Please configure Remora with Firebase Cloud Messaging to start using it.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(20.dp, 0.dp)
            )
        }

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
            var openDialog by remember { mutableStateOf(false) }

            if (openDialog) {
                ResetRemoraDialog(
                    onDismissRequest = { openDialog = false },
                    onConfirm = {
                        viewModel.resetRemora()
                        openDialog = false
                    }
                )
            }

            if (fcmProjectId != null) {
                OutlinedButton(
                    onClick = {
                        openDialog = true
                    }
                ) {
                    Text("Reset")
                }
            } else {
                Button(
                    onClick = { startConfiguration() }
                ) {
                    Text("Configure")
                }
            }
        }
    }
}
