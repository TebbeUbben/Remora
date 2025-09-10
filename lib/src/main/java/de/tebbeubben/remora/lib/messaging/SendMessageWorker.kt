package de.tebbeubben.remora.lib.messaging

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.ServiceInfo
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import de.tebbeubben.remora.lib.R
import de.tebbeubben.remora.lib.RemoraLib
import de.tebbeubben.remora.lib.persistence.repositories.MessageRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject

internal class SendMessageWorker(
    private val appContext: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(appContext, workerParams) {

    @Inject
    lateinit var messageRepository: MessageRepository
    @Inject
    lateinit var  messageHandler: MessageHandler

    companion object {
        const val UNIQUE_WORK_NAME = "RemoraSendMessageWorker"
        private const val NOTIFICATION_ID = 12695
        private const val CHANNEL_ID = "RemoraSendMessageChannel"
    }

    init {
        Log.e("Timing", "SendMessageWorker: init")
        RemoraLib.component?.inject(this) ?: error("RemoraLib not initialized")
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        createNotificationChannel()

        val foregroundInfo = createForegroundInfo()
        setForeground(foregroundInfo)

        try {
            while (true) {
                val queueEntry = messageRepository.getNextSendQueueEntry() ?: break
                messageHandler.sendMessage(queueEntry.peer, queueEntry.message, queueEntry.collapseKey)
                messageRepository.delete(queueEntry)
            }
        } catch (e: IOException) {
            //TODO: Log
            return@withContext Result.retry()
        } catch (e: Exception) {
            //TODO: Log
            return@withContext Result.failure()
        }

        Result.success()
    }

    override suspend fun getForegroundInfo(): ForegroundInfo = createForegroundInfo()

    private fun createNotificationChannel() {
        val name = "Message Sending Service"
        val descriptionText = "Handles sending of queued messages"
        val importance = NotificationManager.IMPORTANCE_LOW
        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }
        val notificationManager: NotificationManager =
            appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private fun createForegroundInfo(): ForegroundInfo {
        val title = "Sending Messages"
        val message = "Processing message queue..."

        val notification = NotificationCompat.Builder(appContext, CHANNEL_ID)
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
}