package de.tebbeubben.remora

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import de.tebbeubben.remora.lib.LibraryMode
import de.tebbeubben.remora.lib.RemoraLib
import kotlinx.coroutines.runBlocking

@HiltAndroidApp
class RemoraApplication : Application() {

    lateinit var remora: RemoraLib

    override fun onCreate() {
        remora = RemoraLib.initialize(this, LibraryMode.FOLLOWER)
        runBlocking { remora.startup() }
        super.onCreate()
    }

}