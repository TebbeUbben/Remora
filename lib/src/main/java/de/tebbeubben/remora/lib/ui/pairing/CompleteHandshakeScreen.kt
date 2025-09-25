package de.tebbeubben.remora.lib.ui.pairing

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.tebbeubben.remora.lib.R

@Composable
internal fun CompleteHandshakeScreen(
    isLoading: Boolean,
    onComplete: () -> Unit
) {
    Column(
        Modifier
            .padding(16.dp, 0.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                stringResource(R.string.remoraPairing_data_accepted),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(8.dp))
            Text(
                stringResource(R.string.remoraFinish_the_handshake_to_exchange_keys_and_enable_secure_communication),
                style = MaterialTheme.typography.bodyMedium
            )
        }

        ProgressButton(
            onClick = onComplete,
            enabled = !isLoading,
            isLoading = isLoading,
            text = stringResource(R.string.remoraComplete_handshake),
            loadingText = stringResource(R.string.remoraCompleting)
        )
    }
}
