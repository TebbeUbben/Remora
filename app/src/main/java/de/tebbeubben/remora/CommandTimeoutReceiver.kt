package de.tebbeubben.remora

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class CommandTimeoutReceiver : BroadcastReceiver() {

    @Inject
    lateinit var notificationHandler: NotificationHandler

    override fun onReceive(context: Context, intent: Intent?) {
        notificationHandler.onTimeout()
    }
}