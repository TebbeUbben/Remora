package de.tebbeubben.remora.lib.ui.configuration

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.tebbeubben.remora.lib.R

@Composable
internal fun ConfigurationScreen(
    viewModel: ConfigurationViewModel,
    onClose: () -> Unit
) {

    val uiState: ConfigurationViewModel.UiState by viewModel.uiState.collectAsState()

    if (uiState.isSetupComplete) {
        onClose()
    }

    val googleServicesPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) viewModel.loadGoogleServiceJson(uri)
    }

    val serviceAccountPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) viewModel.loadServiceAccountJson(uri)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp, 0.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            stringResource(R.string.remoraComplete_firebase_cloud_messaging_fcm_configuration),
            textAlign = TextAlign.Justify,
            style = MaterialTheme.typography.titleLarge
        )
        Text(
            stringResource(R.string.remoraConfiguration_intro_text),
            textAlign = TextAlign.Justify,
            style = MaterialTheme.typography.bodyMedium
        )

        FilePickerCard(
            title = stringResource(R.string.remoraGoogle_services_json),
            subtitle = stringResource(R.string.remoraFirebase_project_configuration),
            description = stringResource(R.string.remoraGoogle_services_instructions),
            state = uiState.googleServicesState,
            differentProjectIds = uiState.differentProjectIds,
            onPick = {
                googleServicesPicker.launch(arrayOf("application/json"))
            },
        )

        FilePickerCard(
            title = stringResource(R.string.remoraService_account_json),
            subtitle = stringResource(R.string.remoraService_account_credentials),
            description = stringResource(R.string.remoraService_account_instructions),
            state = uiState.serviceAccountState,
            differentProjectIds = uiState.differentProjectIds,
            onPick = {
                serviceAccountPicker.launch(arrayOf("application/json"))
            },
        )

        if (uiState.differentProjectIds) {
            Text(
                text = stringResource(R.string.remoraDifferent_project_ids_error),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Justify,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(12.dp, 0.dp)
            )
        }

        Spacer(Modifier.weight(1f))

        FilledTonalButton(
            onClick = { viewModel.completeSetup() },
            enabled = uiState.ready,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (uiState.ready) stringResource(R.string.remoraContinue_button) else stringResource(R.string.remoraSelect_both_files_to_continue))
        }
    }
}
