package de.tebbeubben.remora.lib

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import de.tebbeubben.remora.lib.model.configuration.NetworkConfiguration
import de.tebbeubben.remora.lib.di.ApplicationContext
import javax.inject.Inject
import dagger.Lazy
import de.tebbeubben.remora.lib.lifecycle.LibraryLifecycleCallback
import de.tebbeubben.remora.lib.lifecycle.LifecycleCallbacks
import de.tebbeubben.remora.lib.persistence.repositories.NetworkConfigurationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Singleton

@Singleton
internal class FirebaseAppProvider @Inject constructor(
    @param:ApplicationContext
    private val context: Context,
    private val lifecycleCallbacks: LifecycleCallbacks,
    private val networkConfigurationRepository: NetworkConfigurationRepository,
) : LibraryLifecycleCallback {

    private val _firebaseAppFlow = MutableStateFlow<FirebaseApp?>(null)

    val firebaseAppFlow = _firebaseAppFlow.asStateFlow()
    val firebaseApp get() = _firebaseAppFlow.value

    override suspend fun onConfigure() {
        val config = networkConfigurationRepository.config ?: return
        _firebaseAppFlow.value = with(config) {
            FirebaseApp.initializeApp(
                context,
                FirebaseOptions.Builder()
                    .setProjectId(projectId)
                    .setApiKey(apiKey)
                    .setApplicationId(applicationId)
                    .setGcmSenderId(gcmSenderId)
                    .build(),
                "remora"
            )
        }
        lifecycleCallbacks.onInitFirebase()
    }

    override suspend fun onShutdown() {
        firebaseApp?.delete()
        _firebaseAppFlow.value = null
    }

    override suspend fun onReset() {
        firebaseApp?.delete()
        _firebaseAppFlow.value = null
    }
}