package de.tebbeubben.remora.lib.ui.pairing

import android.content.ClipData
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.tebbeubben.remora.lib.R
import kotlinx.coroutines.launch

@Composable
internal fun SharePairingDataScreen(pairingData: String) {
    val clipboard = LocalClipboard.current
    val context = LocalContext.current

    Column(
        Modifier
            .padding(16.dp, 0.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Text(
            stringResource(R.string.remoraContinue_on_follower_device),
            style = MaterialTheme.typography.headlineSmall
        )

        Text(
            stringResource(R.string.remoraSend_this_code_to_your_other_device_to_continue_pairing),
            style = MaterialTheme.typography.bodyMedium
        )

        TextField(
            modifier = Modifier
                .weight(1.0f)
                .fillMaxWidth(),
            value = pairingData,
            onValueChange = {},
            readOnly = true,
            shape = MaterialTheme.shapes.medium,
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedContainerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp),
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
            )
        )

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val scope = rememberCoroutineScope()
            OutlinedButton(
                onClick = {
                    scope.launch {
                        clipboard.setClipEntry(
                            ClipEntry(
                                ClipData.newPlainText(
                                    context.getString(R.string.remoraParing_data),
                                    pairingData
                                )
                            )
                        )
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Text(stringResource(R.string.remoraCopy))
            }
            Button(
                onClick = {
                    shareText(context, pairingData)
                },
                modifier = Modifier.weight(1f)
            ) {
                Text(stringResource(R.string.remoraShare))
            }
        }
    }
}

private fun shareText(context: Context, text: String) {
    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(sendIntent, context.getString(R.string.remoraShare_pairing_data)))
}
