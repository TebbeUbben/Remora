package de.tebbeubben.remora.lib.persistence.repositories

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import de.tebbeubben.remora.lib.model.configuration.NetworkConfiguration
import de.tebbeubben.remora.lib.di.ApplicationContext
import de.tebbeubben.remora.lib.util.Crypto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class NetworkConfigurationRepository @Inject constructor(
    @ApplicationContext
    context: Context,
    private val crypto: Crypto
) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("remora_network_config", Context.MODE_PRIVATE)

    private val _configFlow = MutableStateFlow(load())
    val configFlow = _configFlow.asStateFlow()

    val config: NetworkConfiguration? get() = configFlow.value

    private fun load(): NetworkConfiguration? {
        val projectId = prefs.getString("projectId", null) ?: return null
        val privateKeyId = prefs.getString("privateKeyId", null) ?: return null
        val privateKeyBase64 = prefs.getString("privateKey", null) ?: return null
        val tokenUri = prefs.getString("tokenUri", null) ?: return null
        val clientEmail = prefs.getString("clientEmail", null) ?: return null
        val apiKey = prefs.getString("apiKey", null) ?: return null
        val applicationId = prefs.getString("applicationId", null) ?: return null
        val gcmSenderId = prefs.getString("gcmSenderId", null) ?: return null

        return NetworkConfiguration(
            projectId = projectId,
            privateKeyId = privateKeyId,
            privateKey = crypto.decodeRSAPrivateKey(privateKeyBase64.hexToByteArray()),
            tokenUri = tokenUri,
            clientEmail = clientEmail,
            apiKey = apiKey,
            applicationId = applicationId,
            gcmSenderId = gcmSenderId
        )
    }

    fun save(config: NetworkConfiguration?) {
        if (config == null) {
            clear()
        } else{
            _configFlow.value = config
            prefs.edit {
                putString("projectId", config.projectId)
                putString("privateKeyId", config.privateKeyId)
                putString("privateKey", config.privateKey.encoded.toHexString())
                putString("tokenUri", config.tokenUri)
                putString("clientEmail", config.clientEmail)
                putString("apiKey", config.apiKey)
                putString("applicationId", config.applicationId)
                putString("gcmSenderId", config.gcmSenderId)
            }
        }
    }

    fun clear() {
        _configFlow.value = null
        prefs.edit { clear() }
    }
}