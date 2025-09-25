package de.tebbeubben.remora.lib.ui.follower_management

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import de.tebbeubben.remora.lib.R

@Composable
internal fun PairDeviceCard(
    onStartPairing: () -> Unit
) {
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        ListItem(
            leadingContent = {
                Icon(
                    painter = painterResource(R.drawable.add_link_24px),
                    contentDescription = null
                )
            },
            headlineContent = {
                Text(
                    stringResource(R.string.remoraPair_device),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            supportingContent = {
                Text(stringResource(R.string.remoraStart_using_a_new_follower_device))
            }
        )

        Text(
            stringResource(R.string.remoraPairDeviceText),
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
            Button(
                onClick = {onStartPairing() }
            ) {
                Text(stringResource(R.string.remoraStart_pairing))
            }
        }
    }
}
