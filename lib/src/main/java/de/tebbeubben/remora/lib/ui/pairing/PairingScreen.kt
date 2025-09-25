package de.tebbeubben.remora.lib.ui.pairing

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import de.tebbeubben.remora.lib.ui.pairing.PairingViewModel.UiState

@Composable
fun RemoraPairingScreen(
    viewModelStoreOwner: ViewModelStoreOwner,
    onClose: () -> Unit,
) {
    val viewModel: PairingViewModel = viewModel(
        viewModelStoreOwner = viewModelStoreOwner,
        factory = PairingViewModel.provideFactory(null)
    )
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text("Pair Devices")
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier.padding(innerPadding)
        ) {
            PairingScreen(
                viewModel = viewModel,
                onClose = onClose
            )
        }
    }
}

@Composable
internal fun PairingScreen(
    viewModel: PairingViewModel,
    onClose: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when (val state = uiState) {
        is UiState.InitiatePairing    -> InitiatePairingScreen(
            loading = state.isLoading,
            onStart = viewModel::initiatePairing
        )

        is UiState.SubmitPairingData  -> SubmitPairingDataScreen(
            isLoading = state.isLoading,
            error = state.error,
            onSubmit = viewModel::submitPairingData
        )

        is UiState.SharePairingData   -> SharePairingDataScreen(
            pairingData = state.pairingData
        )

        is UiState.CompleteHandshake  -> CompleteHandshakeScreen(
            isLoading = state.isLoading,
            onComplete = viewModel::completeHandshake
        )

        is UiState.Verify             -> VerifyScreen(
            verificationWords = state.verificationWords,
            hasPeerVerified = state.hasPeerVerified,
            isLoading = state.isLoading,
            onConfirm = viewModel::verifyCode,
            onReject = viewModel::rejectCode
        )

        is UiState.Complete           -> CompleteScreen(onClose)
        is UiState.VerificationFailed -> ErrorScreen(
            onRetry = {
                viewModel.retry()
            }
        )
        null                          -> LoadingScreen()
    }
}
