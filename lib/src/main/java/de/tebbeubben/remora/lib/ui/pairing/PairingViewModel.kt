package de.tebbeubben.remora.lib.ui.pairing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.protobuf.InvalidProtocolBufferException
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.tebbeubben.remora.lib.util.Crypto
import de.tebbeubben.remora.lib.LibraryMode
import de.tebbeubben.remora.lib.PeerDeviceManager
import de.tebbeubben.remora.lib.RemoraLib
import de.tebbeubben.remora.lib.util.VerificationString
import de.tebbeubben.remora.lib.configuration.NetworkConfiguration
import de.tebbeubben.remora.lib.model.PeerDevice
import de.tebbeubben.remora.lib.ui.pairing.PairingViewModel.UiState.Complete
import de.tebbeubben.remora.lib.ui.pairing.PairingViewModel.UiState.CompleteHandshake
import de.tebbeubben.remora.lib.ui.pairing.PairingViewModel.UiState.InitiatePairing
import de.tebbeubben.remora.lib.ui.pairing.PairingViewModel.UiState.SharePairingData
import de.tebbeubben.remora.lib.ui.pairing.PairingViewModel.UiState.SubmitPairingData
import de.tebbeubben.remora.lib.ui.pairing.PairingViewModel.UiState.Verify
import de.tebbeubben.remora.proto.PairingData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okio.IOException
import java.security.InvalidKeyException
import java.security.spec.InvalidKeySpecException
import kotlin.io.encoding.Base64

internal class PairingViewModel @AssistedInject constructor(
    private val libraryMode: LibraryMode,
    private val peerDeviceManager: PeerDeviceManager,
    private val verificationString: VerificationString,
    private val crypto: Crypto,
    private val remoraLib: RemoraLib,
    @param:Assisted
    val peerId: Long?,
) : ViewModel() {

    private val _peerId = MutableStateFlow(peerId)

    private val peerDeviceFlow = _peerId.flatMapLatest { currentPeerId ->
        if (currentPeerId != null) {
            peerDeviceManager.getPeerDeviceById(currentPeerId)
        } else if (libraryMode == LibraryMode.FOLLOWER) {
            val mainDevice = peerDeviceManager.getPeerDevices().firstOrNull()
            mainDevice?.let { _peerId.emit(it.id) }
            flowOf(mainDevice)
        } else {
            flowOf(null)
        }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

        fun retry() = {
            viewModelScope.launch {
                _peerId.emit(null)
                _selfManagedState.emit(
                    SelfManagedState(
                        isInitiating = false,
                        isSubmittingPairing = false,
                        isVerifying = false,
                        isCompletingHandshake = false,
                        verificationFailed = false,
                        pairingDataError = null
                    )
                )
                remoraLib.reset()
            }
        }

        private val _selfManagedState = MutableStateFlow(
            SelfManagedState(
                isInitiating = false,
                isSubmittingPairing = false,
                isVerifying = false,
                isCompletingHandshake = false,
                verificationFailed = false,
                pairingDataError = null
            )
        )

        val uiState = peerDeviceFlow.combine(_selfManagedState) { peerDevice, selfManagedState ->
            when (peerDevice) {
                null                      -> when {
                    selfManagedState.verificationFailed -> UiState.VerificationFailed
                    libraryMode == LibraryMode.MAIN     -> InitiatePairing(selfManagedState.isInitiating)
                    else                                -> SubmitPairingData(
                        selfManagedState.isSubmittingPairing,
                        selfManagedState.pairingDataError
                    )
                }

                is PeerDevice.Initiating  -> SharePairingData(
                    Base64.UrlSafe.withPadding(Base64.PaddingOption.ABSENT)
                        .encode(peerDeviceManager.getPairingData(peerDevice))
                )

                is PeerDevice.Handshaking -> CompleteHandshake(selfManagedState.isCompletingHandshake)
                is PeerDevice.Verifying   -> if (selfManagedState.verificationFailed) {
                    UiState.VerificationFailed
                } else {
                    Verify(
                        verificationWords = verificationString.getVerificationWords(
                            peerDevice.verificationData
                        ),
                        hasPeerVerified = peerDevice.hasPeerVerified,
                        isLoading = selfManagedState.isVerifying
                    )
                }

                is PeerDevice.Paired      -> Complete
                is PeerDevice.Stub        -> InitiatePairing(selfManagedState.isInitiating)
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

        fun initiatePairing() {
            _selfManagedState.update { it.copy(isInitiating = true) }
            viewModelScope.launch(Dispatchers.Default) {
                val peerDevice = peerDeviceManager.initiateFollowerPairing()
                _peerId.emit(peerDevice)
            }
        }

        fun submitPairingData(pairingData: String) {
            _selfManagedState.update { it.copy(isSubmittingPairing = true, pairingDataError = null) }
            viewModelScope.launch(Dispatchers.Default) {
                try {
                    val data =
                        Base64.UrlSafe.withPadding(Base64.PaddingOption.ABSENT_OPTIONAL)
                            .decode(pairingData)
                    val pairingData = PairingData.parseFrom(data)
                    if (pairingData.version != 1) {
                        _selfManagedState.update {
                            it.copy(
                                isSubmittingPairing = false,
                                pairingDataError = "Incompatible version"
                            )
                        }
                        return@launch
                    }
                    val fcmPrivateKey = crypto.decodeRSAPrivateKey(pairingData.privateKey.toByteArray())
                    val ecdhPublicKey = crypto.decodeECDHPublicKey(pairingData.publicKey.toByteArray())
                    remoraLib.reset()
                    remoraLib.configure(
                        NetworkConfiguration(
                            projectId = pairingData.projectId,
                            privateKeyId = pairingData.privateKeyId,
                            privateKey = fcmPrivateKey,
                            tokenUri = pairingData.tokenUri,
                            clientEmail = pairingData.clientEmail,
                            apiKey = pairingData.apiKey,
                            applicationId = pairingData.applicationId,
                            gcmSenderId = pairingData.gcmSenderId
                        )
                    )
                    val peerDevice = peerDeviceManager.startPairingAsFollower(
                        followerId = pairingData.followerId,
                        salt = pairingData.salt.toByteArray(),
                        pairingTopic = pairingData.pairingTopic,
                        remotePublicKey = ecdhPublicKey
                    )
                    _peerId.emit(peerDevice)
                } catch (e: IllegalArgumentException) {
                    _selfManagedState.update {
                        it.copy(
                            isSubmittingPairing = false,
                            pairingDataError = e.message ?: "Unknown error"
                        )
                    }
                    // TODO: Log
                } catch (e: InvalidProtocolBufferException) {
                    _selfManagedState.update {
                        it.copy(
                            isSubmittingPairing = false,
                            pairingDataError = e.message ?: "Unknown error"
                        )
                    }
                    // TODO: Log
                } catch (e: InvalidKeySpecException) {
                    _selfManagedState.update {
                        it.copy(
                            isSubmittingPairing = false,
                            pairingDataError = e.message ?: "Unknown error"
                        )
                    }
                    // TODO: Log
                } catch (e: InvalidKeyException) {
                    _selfManagedState.update {
                        it.copy(
                            isSubmittingPairing = false,
                            pairingDataError = e.message ?: "Unknown error"
                        )
                    }
                    // TODO: Log
                }
            }
        }

        fun completeHandshake() {
            _selfManagedState.update { it.copy(isCompletingHandshake = true) }
            viewModelScope.launch(Dispatchers.Default) {
                try {
                    peerDeviceManager.exchangePublicKeyWithMain(_peerId.value!!)
                } catch (e: IOException) {
                    remoraLib.reset()
                    _selfManagedState.update { it.copy(isCompletingHandshake = false) }
                    // TODO: Handle
                }
            }
        }

        fun verifyCode() {
            _selfManagedState.update { it.copy(isVerifying = true) }
            viewModelScope.launch(Dispatchers.Default) {
                peerDeviceManager.verifyPairing(_peerId.value!!)
            }
        }

        fun rejectCode() {
            _selfManagedState.update { it.copy(isVerifying = true) }
            viewModelScope.launch(Dispatchers.Default) {
                peerDeviceManager.delete(_peerId.value!!)
                _selfManagedState.update { it.copy(verificationFailed = true) }
                _peerId.emit(null)
            }
        }

        sealed class UiState {
            data class InitiatePairing(val isLoading: Boolean) : UiState()
            data class SubmitPairingData(val isLoading: Boolean, val error: String?) : UiState()
            data class SharePairingData(val pairingData: String) : UiState()
            data class CompleteHandshake(val isLoading: Boolean) : UiState()
            data class Verify(
                val verificationWords: List<String>,
                val hasPeerVerified: Boolean,
                val isLoading: Boolean,
            ) :
                UiState()

            object Complete : UiState()
            object VerificationFailed : UiState()
        }

        private data class SelfManagedState(
            val isInitiating: Boolean,
            val isSubmittingPairing: Boolean,
            val isCompletingHandshake: Boolean,
            val isVerifying: Boolean,
            val verificationFailed: Boolean,
            val pairingDataError: String?,
        )

        companion object {
        fun provideFactory(peerId: Long?): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val component = RemoraLib.component
                        ?: error("RemoraLib component not initialized")
                    return component.pairingViewModelFactory().create(peerId) as T
                }
            }
    }
    }

    @AssistedFactory
    internal interface PairingViewModelFactory {

        fun create(peerId: Long?): PairingViewModel
    }