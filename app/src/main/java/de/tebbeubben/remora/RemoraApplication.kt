package de.tebbeubben.remora

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import de.tebbeubben.remora.lib.RemoraLib
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltAndroidApp
class RemoraApplication : Application() {

    @Inject
    lateinit var remora: RemoraLib

    @Inject
    lateinit var notificationHandler: NotificationHandler

    override fun onCreate() {
        super.onCreate()
        runBlocking { remora.startup() }
        notificationHandler.start()
    }

}