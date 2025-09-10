package de.tebbeubben.remora.lib.ui.follower_management

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
internal fun FollowerManagementScreen(
    viewModel: FollowerManagementViewModel,
    onStartPairing: (id: Long?) -> Unit,
    startConfiguration: () -> Unit
) {
    val peerDevices by viewModel.peerDevices.collectAsState()
    val fcmProjectId by viewModel.fcmProjectId.collectAsState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        for (peerDevice in peerDevices) {
            PeerDeviceCard(peerDevice = peerDevice, viewModel = viewModel, onStartPairing = onStartPairing)
        }

        if (fcmProjectId != null) {
            PairDeviceCard(
                onStartPairing = { onStartPairing(null) }
            )
        }

        FcmConfigurationCard(
            fcmProjectId = fcmProjectId,
            viewModel = viewModel,
            startConfiguration = startConfiguration
        )
    }
}
