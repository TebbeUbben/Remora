package de.tebbeubben.remora.lib.messaging

import android.annotation.SuppressLint
import android.content.Intent
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import de.tebbeubben.remora.lib.RemoraLib
import kotlinx.coroutines.runBlocking

@SuppressLint("MissingFirebaseInstanceTokenRefresh")
class RemoraMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        Log.d("MessagingService", "onMessageReceived: ${message.from}")
        runBlocking { RemoraLib.Companion.instance.onReceiveRemoteMessage(message) }
    }

    override fun handleIntent(intent: Intent) {
        // Remove these keys, so that Firebase doesn't handle notification payloads.
        // See https://github.com/firebase/firebase-android-sdk/blob/main/firebase-messaging/src/main/java/com/google/firebase/messaging/NotificationParams.java#L419
        // This works in conjunction with disabling notification delegation in the manifest.
        intent.removeExtra("gcm.n.e")
        intent.removeExtra("gcm.notification.e")
        super.handleIntent(intent)
    }
}