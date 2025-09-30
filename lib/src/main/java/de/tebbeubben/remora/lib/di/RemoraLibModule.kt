package de.tebbeubben.remora.lib.di

import android.content.Context
import androidx.room.Room
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoSet
import de.tebbeubben.remora.lib.FirebaseAppProvider
import de.tebbeubben.remora.lib.PeerDeviceManager
import de.tebbeubben.remora.lib.RemoraLib
import de.tebbeubben.remora.lib.lifecycle.LibraryLifecycleCallback
import de.tebbeubben.remora.lib.messaging.FcmSubscriptionManager
import de.tebbeubben.remora.lib.persistence.RemoraLibDatabase
import de.tebbeubben.remora.lib.persistence.daos.MessageIdCounterDao
import de.tebbeubben.remora.lib.persistence.daos.PeerDao
import de.tebbeubben.remora.lib.persistence.daos.SendQueueDao
import de.tebbeubben.remora.lib.persistence.repositories.NetworkConfigurationRepository
import de.tebbeubben.remora.lib.status.StatusManager
import javax.inject.Singleton

@Module(includes = [RemoraLibModule.Bindings::class])
internal object RemoraLibModule {

    @Module
    interface Bindings {

        @Binds
        @IntoSet
        fun bindsFcmSubscriptionManager(fcmSubscriptionManager: FcmSubscriptionManager): LibraryLifecycleCallback

        @Binds
        @IntoSet
        fun bindsFirebaseAppProvider(firebaseAppProvider: FirebaseAppProvider): LibraryLifecycleCallback

        @Binds
        @IntoSet
        fun bindsPeerDeviceManager(peerDeviceManager: PeerDeviceManager): LibraryLifecycleCallback

        @Binds
        @IntoSet
        fun bindsNetworkConfigurationRepository(networkConfigurationRepository: NetworkConfigurationRepository): LibraryLifecycleCallback

        @Binds
        @IntoSet
        fun bindsStatusManager(statusManager: StatusManager): LibraryLifecycleCallback

        @Binds
        @IntoSet
        fun bindsRemoraLib(remoraLib: RemoraLib): LibraryLifecycleCallback
    }

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): RemoraLibDatabase =
        Room.databaseBuilder(
            context,
            RemoraLibDatabase::class.java,
            RemoraLibDatabase.DATABASE_NAME
        ).build()

    @Provides
    @Singleton
    fun providePeerDao(database: RemoraLibDatabase): PeerDao = database.peerDao()

    @Provides
    @Singleton
    fun provideMessageIdCounterDao(database: RemoraLibDatabase): MessageIdCounterDao = database.messageIdCounterDao()

    @Provides
    @Singleton
    fun providesSendQueueDao(database: RemoraLibDatabase): SendQueueDao = database.sendQueueDao()
}