package de.tebbeubben.remora.lib

import android.content.Context
import com.google.firebase.messaging.RemoteMessage
import de.tebbeubben.remora.lib.commands.CommandHandler
import de.tebbeubben.remora.lib.commands.CommandRequester
import de.tebbeubben.remora.lib.commands.CommandProcessor
import de.tebbeubben.remora.lib.model.configuration.NetworkConfiguration
import de.tebbeubben.remora.lib.persistence.repositories.NetworkConfigurationRepository
import de.tebbeubben.remora.lib.di.DaggerRemoraLibComponent
import de.tebbeubben.remora.lib.di.InitModule
import de.tebbeubben.remora.lib.di.RemoraLibComponent
import de.tebbeubben.remora.lib.messaging.MessageHandler
import de.tebbeubben.remora.lib.model.commands.RemoraCommandData
import de.tebbeubben.remora.lib.persistence.RemoraLibDatabase
import de.tebbeubben.remora.lib.model.status.RemoraStatusData
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
    private val commandProcessor: CommandProcessor
) {

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

    val statusFlow get() = statusManager.statusFlow

    val commandStateFlow get() = commandRequester.commandStateFlow

    fun setCommandHandler(handler: CommandHandler) {
        commandProcessor.commandHandler = handler
    }

    suspend fun clearCommand() = commandRequester.clear()
    suspend fun initiateCommand(commandData: RemoraCommandData) = commandRequester.initiateCommand(commandData)
    suspend fun prepareCommand() = commandRequester.sendPrepareRequest()
    suspend fun confirmCommand() = commandRequester.sendConfirmation()

    suspend fun shareStatus(statusData: RemoraStatusData) = statusManager.shareStatus(statusData)

    //TODO: Lifecycle callbacks
    suspend fun reset() {
        networkConfigurationRepository.clear()
        withContext(Dispatchers.IO) {
            firebaseAppProvider.shutdown()
            database.clearAllTables()
            peerDeviceManager.reset()
        }
    }

    internal suspend fun configure(config: NetworkConfiguration) {
        if (networkConfigurationRepository.config != null) {
            error("RemoraLib is already configured. Run reset() first if needed.")
        }
        networkConfigurationRepository.save(config)
        firebaseAppProvider.startupWithConfig(config)
    }

    suspend fun startup() {
        networkConfigurationRepository.config?.let {
            firebaseAppProvider.startupWithConfig(it)
        }
        peerDeviceManager.startup()
    }

    suspend fun shutdown() {
        firebaseAppProvider.shutdown()
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