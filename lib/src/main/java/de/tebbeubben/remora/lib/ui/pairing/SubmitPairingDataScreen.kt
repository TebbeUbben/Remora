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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.tebbeubben.remora.lib.R

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
            stringResource(R.string.remoraSubmit_pairing_data),
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(8.dp))

        Text(
            stringResource(R.string.remoraPlease_initiate_pairing_on_the_main_phone_and_paste_the_pairing_data_here),
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(16.dp))

        if (error != null) {

            Text(
                stringResource(R.string.remoraThere_seems_to_be_something_wrong_with_the_provided_pairing_data, error),
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
            label = { Text(stringResource(R.string.remoraPairing_data)) },
            enabled = !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            placeholder = { Text(stringResource(R.string.remoraPaste_here)) },
        )

        Spacer(Modifier.height(16.dp))

        ProgressButton(
            onClick = { onSubmit(text.trim()) },
            enabled = !isLoading && text.isNotBlank(),
            isLoading = isLoading,
            text = stringResource(R.string.remoraSubmit),
            loadingText = stringResource(R.string.remoraSubmitting)
        )
    }
}
