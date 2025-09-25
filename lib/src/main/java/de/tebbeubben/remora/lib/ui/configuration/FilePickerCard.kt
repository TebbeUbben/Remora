package de.tebbeubben.remora.lib.ui.configuration

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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import de.tebbeubben.remora.lib.R

@Composable
internal fun FilePickerCard(
    title: String,
    subtitle: String,
    description: String,
    state: ConfigurationViewModel.JsonState,
    differentProjectIds: Boolean,
    onPick: () -> Unit,
) {
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        ListItem(
            leadingContent = {
                Icon(
                    painter = painterResource(R.drawable.description_24px),
                    contentDescription = null
                )
            },
            headlineContent = {
                Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis)
            },
            supportingContent = {
                Text(subtitle)
            },
            trailingContent = { StateIndicator(state) }
        )

        if (state !is ConfigurationViewModel.JsonState.Empty) { // Assuming an Idle state
            StateMessage(
                state = state,
                differentProjectIds = differentProjectIds
            )
        }

        Text(
            description,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Justify,
            modifier = Modifier.padding(12.dp, 0.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.End),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onPick
            ) {
                Text(
                    if (state !is ConfigurationViewModel.JsonState.Success) stringResource(R.string.remoraSelect_file) else stringResource(
                        R.string.remoraChange_file
                    )
                )
            }
        }
    }
}
