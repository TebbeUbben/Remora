package de.tebbeubben.remora.lib.ui.follower_management

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import de.tebbeubben.remora.lib.PeerDeviceManager
import de.tebbeubben.remora.lib.RemoraLib
import de.tebbeubben.remora.lib.persistence.repositories.NetworkConfigurationRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

internal class FollowerManagementViewModel @Inject constructor(
    private val peerDeviceManager: PeerDeviceManager,
    private val networkConfigurationRepository: NetworkConfigurationRepository,
    private val remoraLib: RemoraLib
) : ViewModel() {

    val peerDevices = peerDeviceManager.getPeerDevicesFlow().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val fcmProjectId = networkConfigurationRepository.configFlow.map { it?.projectId }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    fun resetRemora() {
        viewModelScope.launch {
            remoraLib.reset()
        }
    }

    fun deletePeerDevice(peerDeviceId: Long) {
        viewModelScope.launch {
            peerDeviceManager.delete(peerDeviceId)
        }
    }

    fun renameDevice(peerDeviceId: Long, newName: String) {
        viewModelScope.launch {
            peerDeviceManager.renameDevice(peerDeviceId, newName)
        }
    }

    companion object {

        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val component = RemoraLib.component
                    ?: error("RemoraLib component not initialized")
                return component.followerManagementViewModel() as T
            }
        }
    }
}