package de.tebbeubben.remora

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import de.tebbeubben.remora.lib.RemoraLib
import de.tebbeubben.remora.util.formatBG
import de.tebbeubben.remora.util.formatCarbs
import de.tebbeubben.remora.util.formatInsulin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StatusNotificationManager @Inject constructor(
    private val remoraLib: RemoraLib,
    @param:ApplicationContext
    private val context: Context
) : CoroutineScope by CoroutineScope(Dispatchers.Main) {

    companion object {
        const val CHANNEL_ID = "remora_status"
        const val NOTIFICATION_ID = 51235
    }

    fun start() {
        val notificationManager = context.getSystemService(NotificationManager::class.java)

        val channel = NotificationChannel(CHANNEL_ID, "Remora Status", NotificationManager.IMPORTANCE_DEFAULT).apply {
            this.description = "Shows the current status of the paired AndroidAPS device."
            this.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            this.enableVibration(false)
            this.enableLights(false)
            this.setSound(null, null)
            this.setShowBadge(false)
        }

        notificationManager.createNotificationChannel(channel)

        launch {
            remoraLib.passiveStatusFlow.collectLatest { statusView ->
                val data = statusView.short?.data
                if (data == null) {
                    notificationManager.cancel(NOTIFICATION_ID)
                    return@collectLatest
                }

                val bg = data.displayBg?.value?.formatBG(data.usesMgdl)
                val delta = data.displayBg?.deltas?.delta?.let { delta -> (if (delta >= 0f) "+" else "") + delta.formatBG(data.usesMgdl) }
                val iob = (data.basalIob + data.bolusIob).formatInsulin() + " U"
                val cob = data.displayCob?.formatCarbs()?.plus(" g") ?: "n/a"

                val title = StringBuilder()
                if (bg != null) title.append(bg)
                else title.append("No BG data")
                if (delta != null) title.append(" $delta")

                val text = StringBuilder()
                text.append("IOB: $iob")
                text.append("  COB: $cob")

                val notification = Notification.Builder(context, CHANNEL_ID)
                    .setOngoing(true)
                    .setSmallIcon(R.drawable.remora_logo)
                    .setCategory(Notification.CATEGORY_STATUS)
                    .setVisibility(Notification.VISIBILITY_PUBLIC)
                    .setShowWhen(true)
                    .setWhen(data.timestamp.toEpochMilliseconds())
                    .setContentTitle(title.toString())
                    .setContentText(text.toString())
                    .build()

                notificationManager.notify(NOTIFICATION_ID, notification)
            }
        }
    }

}