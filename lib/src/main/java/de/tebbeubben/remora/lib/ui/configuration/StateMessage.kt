package de.tebbeubben.remora.lib.ui.configuration

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.tebbeubben.remora.lib.R

@Composable
internal fun StateMessage(
    state: ConfigurationViewModel.JsonState,
    differentProjectIds: Boolean
) {
    when (state) {
        is ConfigurationViewModel.JsonState.Success -> {
            Text(
                stringResource(R.string.remoraProject_id, state.projectId),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Justify,
                color = if (differentProjectIds) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(12.dp, 0.dp)
            )
            Spacer(Modifier.height(16.dp))
        }

        is ConfigurationViewModel.JsonState.WrongFile -> {
            Text(
                stringResource(
                    R.string.remoraThis_is_not_a_valid_configuration_file,
                    state.message.toString()
                ),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Justify,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(12.dp, 0.dp)
            )
            Spacer(Modifier.height(16.dp))
        }

        is ConfigurationViewModel.JsonState.UnknownError -> {
            Text(
                stringResource(R.string.remoraAn_error_occurred_while_loading_the_file, state.message),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Justify,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(12.dp, 0.dp)
            )
            Spacer(Modifier.height(16.dp))
        }

        else -> {}
    }
}
