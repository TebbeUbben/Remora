package de.tebbeubben.remora

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import de.tebbeubben.remora.lib.RemoraLib
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
class DiscardCommandReceiver : BroadcastReceiver() {

    @Inject
    lateinit var remoraLib: RemoraLib

    override fun onReceive(context: Context, intent: Intent?) {
        runBlocking { remoraLib.clearCommand() }
    }
}