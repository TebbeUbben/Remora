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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

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
                "Pairing data accepted ✅",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Finish the handshake to exchange keys and enable secure communication.",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        ProgressButton(
            onClick = onComplete,
            enabled = !isLoading,
            isLoading = isLoading,
            text = "Complete handshake",
            loadingText = "Completing…"
        )
    }
}
