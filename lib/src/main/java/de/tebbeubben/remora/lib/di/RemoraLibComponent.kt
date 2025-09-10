package de.tebbeubben.remora.lib.di

import dagger.Component
import de.tebbeubben.remora.lib.RemoraLib
import de.tebbeubben.remora.lib.messaging.SendMessageWorker
import de.tebbeubben.remora.lib.status.UploadStatusWorker
import de.tebbeubben.remora.lib.ui.configuration.ConfigurationViewModel
import de.tebbeubben.remora.lib.ui.follower_management.FollowerManagementViewModel
import de.tebbeubben.remora.lib.ui.pairing.PairingViewModelFactory
import javax.inject.Singleton

@Component(modules = [RemoraLibModule::class, InitModule::class])
@Singleton
internal interface RemoraLibComponent {
    fun remora(): RemoraLib

    fun configurationViewModel(): ConfigurationViewModel

    fun pairingViewModelFactory(): PairingViewModelFactory

    fun followerManagementViewModel(): FollowerManagementViewModel

    fun inject(sendMessageWorker: SendMessageWorker)

    fun inject(uploadStatusWorker: UploadStatusWorker)

    @Component.Builder
    interface Builder {
        fun build(): RemoraLibComponent
        fun initModule(module: InitModule): Builder
    }
}