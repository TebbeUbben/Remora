package de.tebbeubben.remora.lib.di

import android.content.Context
import androidx.room.Room
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoSet
import de.tebbeubben.remora.lib.messaging.FcmSubscriptionManager
import de.tebbeubben.remora.lib.FirebaseAppProvider
import de.tebbeubben.remora.lib.status.StatusManager
import de.tebbeubben.remora.lib.persistence.RemoraLibDatabase
import de.tebbeubben.remora.lib.persistence.daos.MessageIdCounterDao
import de.tebbeubben.remora.lib.persistence.daos.PeerDao
import de.tebbeubben.remora.lib.persistence.daos.SendQueueDao
import javax.inject.Singleton

@Module(includes = [RemoraLibModule.Bindings::class])
internal object RemoraLibModule {

    @Module
    interface Bindings {
        @Binds
        @IntoSet
        fun bindsFcmSubscriptionManager(fcmSubscriptionManager: FcmSubscriptionManager): FirebaseAppProvider.Hook

        @Binds
        @IntoSet
        fun bindsStatusManager(statusManager: StatusManager): FirebaseAppProvider.Hook
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