package de.tebbeubben.remora.lib.ui.configuration

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import de.tebbeubben.remora.lib.RemoraLib
import de.tebbeubben.remora.lib.configuration.GoogleServicesJson
import de.tebbeubben.remora.lib.configuration.NetworkConfiguration
import de.tebbeubben.remora.lib.configuration.ServiceAccountJson
import de.tebbeubben.remora.lib.di.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

internal class ConfigurationViewModel @Inject constructor(
    @param:ApplicationContext
    private val appContext: Context,
    private val googleServicesJsonParser: GoogleServicesJson,
    private val serviceAccountJsonParser: ServiceAccountJson,
    private val remoraLib: RemoraLib
) : ViewModel() {

    private var currentGoogleServices: GoogleServicesJson.Data? = null
    private var currentServiceAccount: ServiceAccountJson.Data? = null

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    fun completeSetup() {
        viewModelScope.launch {
            val googleServices = currentGoogleServices
            val serviceAccount = currentServiceAccount

            if (googleServices != null && serviceAccount != null &&
                googleServices.projectId == serviceAccount.projectId
            ) {
                _uiState.update { it.copy(ready = false) }
                remoraLib.configure(
                    NetworkConfiguration(
                        projectId = serviceAccount.projectId,
                        privateKeyId = serviceAccount.privateKeyId,
                        privateKey = serviceAccount.privateKey,
                        tokenUri = serviceAccount.tokenUri,
                        clientEmail = serviceAccount.clientEmail,
                        apiKey = googleServices.apiKey,
                        applicationId = googleServices.applicationId,
                        gcmSenderId = googleServices.gcmSenderId
                    )
                )
                _uiState.update { it.copy(isSetupComplete = true) }
            }
        }
    }

    fun loadGoogleServiceJson(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(googleServicesState = JsonState.Loading) }

            val fileSize = getFileSize(uri)
            if (fileSize == null || fileSize >= 10 * 1024) { // 10KB limit
                currentGoogleServices = null
                _uiState.update {
                    it.copy(
                        googleServicesState = JsonState.UnknownError("File too large (max 10KB)"),
                        // Recalculate readiness and project ID diff
                        differentProjectIds = checkDifferentProjectIds(null, currentServiceAccount),
                        ready = false
                    )
                }
                return@launch
            }

            val fileContent = readFileContent(uri)
            if (fileContent == null) {
                currentGoogleServices = null
                _uiState.update {
                    it.copy(
                        googleServicesState = JsonState.UnknownError("Can't read file"),
                        differentProjectIds = checkDifferentProjectIds(null, currentServiceAccount),
                        ready = false
                    )
                }
                return@launch
            }

            try {
                val parsedData = googleServicesJsonParser.parseFromJson(fileContent)
                currentGoogleServices = parsedData
                val differentIds = checkDifferentProjectIds(parsedData, currentServiceAccount)
                val isReady = !differentIds && currentServiceAccount != null

                _uiState.update {
                    it.copy(
                        googleServicesState = JsonState.Success(parsedData.projectId),
                        differentProjectIds = differentIds,
                        ready = isReady
                    )
                }
            } catch (e: IllegalArgumentException) {
                currentGoogleServices = null
                // TODO: Log e
                _uiState.update {
                    it.copy(
                        googleServicesState = JsonState.WrongFile(e.message ?: "Invalid google-services.json format"),
                        differentProjectIds = checkDifferentProjectIds(null, currentServiceAccount),
                        ready = false
                    )
                }
            }
        }
    }

    fun loadServiceAccountJson(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(serviceAccountState = JsonState.Loading) }

            val fileSize = getFileSize(uri)
            if (fileSize == null || fileSize >= 10 * 1024) { // 10KB limit
                currentServiceAccount = null
                _uiState.update {
                    it.copy(
                        serviceAccountState = JsonState.UnknownError("File too large (max 10KB)"),
                        differentProjectIds = checkDifferentProjectIds(currentGoogleServices, null),
                        ready = false
                    )
                }
                return@launch
            }

            val fileContent = readFileContent(uri)
            if (fileContent == null) {
                currentServiceAccount = null
                _uiState.update {
                    it.copy(
                        serviceAccountState = JsonState.UnknownError("Can't read file"),
                        differentProjectIds = checkDifferentProjectIds(currentGoogleServices, null),
                        ready = false
                    )
                }
                return@launch
            }

            try {
                val parsedData = serviceAccountJsonParser.parseFromJson(fileContent)
                currentServiceAccount = parsedData
                val differentIds = checkDifferentProjectIds(currentGoogleServices, parsedData)
                val isReady = !differentIds && currentGoogleServices != null

                _uiState.update {
                    it.copy(
                        serviceAccountState = JsonState.Success(parsedData.projectId),
                        differentProjectIds = differentIds,
                        ready = isReady
                    )
                }
            } catch (e: IllegalArgumentException) {
                currentServiceAccount = null
                // TODO: Log e
                _uiState.update {
                    it.copy(
                        serviceAccountState = JsonState.WrongFile(e.message ?: "Invalid service-account.json format"),
                        differentProjectIds = checkDifferentProjectIds(currentGoogleServices, null),
                        ready = false
                    )
                }
            }
        }
    }

    private fun checkDifferentProjectIds(
        googleServices: GoogleServicesJson.Data?,
        serviceAccount: ServiceAccountJson.Data?
    ): Boolean {
        if (googleServices == null || serviceAccount == null) {
            return false
        }
        return googleServices.projectId != serviceAccount.projectId
    }

    private fun getFileSize(uri: Uri): Long? =
        appContext.contentResolver.query(
            uri,
            arrayOf(OpenableColumns.SIZE),
            null,
            null,
            null
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (sizeIndex != -1 && !cursor.isNull(sizeIndex)) cursor.getLong(sizeIndex) else null
            } else null
        }

    private fun readFileContent(uri: Uri): String? =
        try {
            appContext.contentResolver.openInputStream(uri)?.use { it.readBytes().decodeToString() }
        } catch (e: Exception) {
            // TODO: Log e (IOException, SecurityException etc.)
            null
        }

    data class UiState(
        val googleServicesState: JsonState = JsonState.Empty,
        val serviceAccountState: JsonState = JsonState.Empty,
        val differentProjectIds: Boolean = false,
        val ready: Boolean = false,
        val isSetupComplete: Boolean = false
    )

    sealed class JsonState {
        object Empty : JsonState()
        object Loading : JsonState()
        data class Success(val projectId: String) : JsonState()
        data class WrongFile(val message: String?) : JsonState()
        data class UnknownError(val message: String) : JsonState()
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val component = RemoraLib.component
                    ?: error("RemoraLib component not initialized")
                return component.configurationViewModel() as T
            }
        }
    }
}