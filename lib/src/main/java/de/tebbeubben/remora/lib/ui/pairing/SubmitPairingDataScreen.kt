package de.tebbeubben.remora.lib.ui.pairing

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
internal fun SubmitPairingDataScreen(
    isLoading: Boolean,
    error: String?,
    onSubmit: (String) -> Unit
) {
    var text by rememberSaveable { mutableStateOf("") }

    Column(
        Modifier
            .padding(16.dp, 0.dp)
            .fillMaxSize()
    ) {
        Text(
            "Submit Pairing Data",
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(8.dp))

        Text(
            "Please initiate pairing on the main phone and paste the pairing data here:",
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(8.dp))

        if (error != null) {

            Text(
                "There seems to be something wrong with the provided pairing data: $error",
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.error
            )

            Spacer(Modifier.height(8.dp))
        }

        TextField(
            value = text,
            onValueChange = { text = it },
            label = { Text("Pairing Data") },
            enabled = !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            placeholder = { Text("Paste here…") },
        )

        Spacer(Modifier.height(16.dp))

        ProgressButton(
            onClick = { onSubmit(text.trim()) },
            enabled = !isLoading && text.isNotBlank(),
            isLoading = isLoading,
            text = "Submit",
            loadingText = "Submitting…"
        )
    }
}
