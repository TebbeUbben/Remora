package de.tebbeubben.remora.lib

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import de.tebbeubben.remora.lib.model.configuration.NetworkConfiguration
import de.tebbeubben.remora.lib.di.ApplicationContext
import javax.inject.Inject
import dagger.Lazy
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Singleton

@Singleton
internal class FirebaseAppProvider @Inject constructor(
    @param:ApplicationContext
    private val context: Context,
    private val hooks: Lazy<Set<@JvmSuppressWildcards Hook>>
) {

    private val _firebaseAppFlow = MutableStateFlow<FirebaseApp?>(null)

    val firebaseAppFlow = _firebaseAppFlow.asStateFlow()
    val firebaseApp get() = _firebaseAppFlow.value

    suspend fun startupWithConfig(config: NetworkConfiguration) {
        with(config) {
            val firebaseApp = FirebaseApp.initializeApp(
                context,
                FirebaseOptions.Builder()
                    .setProjectId(projectId)
                    .setApiKey(apiKey)
                    .setApplicationId(applicationId)
                    .setGcmSenderId(gcmSenderId)
                    .build(),
                "remora"
            )
            _firebaseAppFlow.value = firebaseApp
            hooks.get().forEach { it.onStartup() }
        }
    }

    suspend fun shutdown() {
        firebaseApp?.delete()
        hooks.get().forEach { it.onShutdown() }
        _firebaseAppFlow.value = null
    }

    interface Hook {

        suspend fun onStartup() {

        }
        suspend fun onShutdown() {

        }
    }
}