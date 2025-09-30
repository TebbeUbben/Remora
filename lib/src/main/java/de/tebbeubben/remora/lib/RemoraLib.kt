package de.tebbeubben.remora.lib

import android.content.Context
import com.google.firebase.messaging.RemoteMessage
import dagger.Lazy
import de.tebbeubben.remora.lib.commands.CommandHandler
import de.tebbeubben.remora.lib.commands.CommandProcessor
import de.tebbeubben.remora.lib.commands.CommandRequester
import de.tebbeubben.remora.lib.di.DaggerRemoraLibComponent
import de.tebbeubben.remora.lib.di.InitModule
import de.tebbeubben.remora.lib.di.RemoraLibComponent
import de.tebbeubben.remora.lib.lifecycle.LibraryLifecycleCallback
import de.tebbeubben.remora.lib.lifecycle.LifecycleCallbacks
import de.tebbeubben.remora.lib.messaging.MessageHandler
import de.tebbeubben.remora.lib.model.commands.RemoraCommandData
import de.tebbeubben.remora.lib.model.configuration.NetworkConfiguration
import de.tebbeubben.remora.lib.model.status.RemoraStatusData
import de.tebbeubben.remora.lib.persistence.RemoraLibDatabase
import de.tebbeubben.remora.lib.persistence.repositories.NetworkConfigurationRepository
import de.tebbeubben.remora.lib.status.StatusManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.io.encoding.Base64

@Singleton
class RemoraLib @Inject internal constructor(
    private val networkConfigurationRepository: NetworkConfigurationRepository,
    private val firebaseAppProvider: FirebaseAppProvider,
    private val messageHandler: MessageHandler,
    private val database: RemoraLibDatabase,
    private val statusManager: StatusManager,
    private val peerDeviceManager: PeerDeviceManager,
    private val commandRequester: CommandRequester,
    private val commandProcessor: CommandProcessor,
    private val lifecycleCallbacks: LifecycleCallbacks
) : LibraryLifecycleCallback {

    suspend fun onReceiveRemoteMessage(remoteMessage: RemoteMessage) {
        firebaseAppProvider.firebaseApp?.let { firebaseApp ->
            if (remoteMessage.senderId == firebaseApp.options.gcmSenderId) {
                val data = remoteMessage.data.getOrElse("") { null } ?: return
                try {
                    val decoded = Base64.UrlSafe.withPadding(Base64.PaddingOption.ABSENT_OPTIONAL).decode(data)
                    messageHandler.onReceiveData(remoteMessage.from ?: return, decoded)
                } catch (e: IllegalArgumentException) {
                    //TODO: Log
                }
            }
        }
    }

    val isPairedToMain get() = peerDeviceManager.isPairedToMain

    val activeStatusFlow get() = statusManager.activeStatusFlow
    val passiveStatusFlow get() = statusManager.passiveStatusFlow

    val commandStateFlow get() = commandRequester.commandStateFlow

    fun setCommandHandler(handler: CommandHandler) {
        commandProcessor.commandHandler = handler
    }

    suspend fun invalidateCurrentCommand() = commandProcessor.invalidateCurrentCommand()

    suspend fun clearCommand() = commandRequester.clear()
    suspend fun initiateCommand(commandData: RemoraCommandData) = commandRequester.initiateCommand(commandData)
    suspend fun prepareCommand() = commandRequester.sendPrepareRequest()
    suspend fun confirmCommand() = commandRequester.sendConfirmation()

    suspend fun shareStatus(statusData: RemoraStatusData) = statusManager.shareStatus(statusData)


    suspend fun startup() = lifecycleCallbacks.onStartup()

    internal suspend fun configure(config: NetworkConfiguration) {
        networkConfigurationRepository.save(config)
    }

    suspend fun shutdown() = lifecycleCallbacks.onShutdown()

    suspend fun reset() = lifecycleCallbacks.onReset()

    override suspend fun onReset() {
        withContext(Dispatchers.IO) {
            database.clearAllTables()
        }
    }

    companion object {

        internal var component: RemoraLibComponent? = null
        val initialized get() = component != null

        fun initialize(context: Context, mode: LibraryMode): RemoraLib {
            if (component != null) error("RemoraLib is already initialized.")
            val newComponent = DaggerRemoraLibComponent.builder()
                .initModule(InitModule(context.applicationContext, mode))
                .build()
            component = newComponent
            return newComponent.remora()
        }

        val instance
            get() = component?.remora()
                ?: error("RemoraLib is not initialized. Call initialize first.")
    }
}