package de.tebbeubben.remora.lib.status

import android.content.Context
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import de.tebbeubben.remora.lib.R
import de.tebbeubben.remora.lib.RemoraNotificationChannel
import javax.inject.Inject

internal abstract class AbstractStatusWorker(
    private val appContext: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val UNIQUE_WORK_NAME = "RemoraUploadStatusWorker"
        private const val NOTIFICATION_ID = 12696
    }

    @Inject
    lateinit var remoraNotificationChannel: RemoraNotificationChannel

    override suspend fun getForegroundInfo(): ForegroundInfo = createForegroundInfo()

    private fun createForegroundInfo(): ForegroundInfo {
        val title = appContext.getString(R.string.remoraUploading_status)
        val message = appContext.getString(R.string.remoraUploading_status_to_remote_devices)

        val notification = NotificationCompat.Builder(appContext, remoraNotificationChannel.channelId)
            .setContentTitle(title)
            .setTicker(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.remora_logo)
            .setOngoing(true)
            .build()

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            ForegroundInfo(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            ForegroundInfo(NOTIFICATION_ID, notification)
        }
    }

    override suspend fun doWork(): Result {
        remoraNotificationChannel.createNotificationChannel()
        val foregroundInfo = createForegroundInfo()
        setForeground(foregroundInfo)
        return doWorkImpl()
    }

    abstract suspend fun doWorkImpl(): Result
}