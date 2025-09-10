package de.tebbeubben.remora.lib.ui.configuration

import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import de.tebbeubben.remora.lib.R

@Composable
internal fun StateIndicator(state: ConfigurationViewModel.JsonState) {
    when (state) {
        is ConfigurationViewModel.JsonState.Success -> Icon(
            painter = painterResource(R.drawable.check_circle_24px),
            tint = MaterialTheme.colorScheme.primary,
            contentDescription = null
        )

        is ConfigurationViewModel.JsonState.Loading -> CircularProgressIndicator(
            modifier = Modifier.size(24.dp),
            strokeWidth = 3.dp
        )

        is ConfigurationViewModel.JsonState.UnknownError,
        is ConfigurationViewModel.JsonState.WrongFile -> Icon(
            painter = painterResource(R.drawable.cancel_24px),
            tint = MaterialTheme.colorScheme.error,
            contentDescription = null
        )

        else -> {}
    }
}
