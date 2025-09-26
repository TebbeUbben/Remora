package de.tebbeubben.remora.lib

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import dagger.Reusable
import de.tebbeubben.remora.lib.di.ApplicationContext
import javax.inject.Inject

@Reusable
internal class RemoraNotificationChannel @Inject constructor(
    @param:ApplicationContext val context: Context
) {
    val channelId = "remora_notification_channel"

    fun createNotificationChannel() {
        val name = context.getString(R.string.remoraRemora)
        val descriptionText = context.getString(R.string.remoraHandles_communication_with_follower_devices)
        val importance = NotificationManager.IMPORTANCE_LOW
        val channel = NotificationChannel(channelId, name, importance).apply {
            description = descriptionText
            enableVibration(false)
            setShowBadge(false)
            enableLights(false)
            setSound(null, null)
        }
        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}