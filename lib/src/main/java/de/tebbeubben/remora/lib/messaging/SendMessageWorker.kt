package de.tebbeubben.remora.lib.messaging

import android.content.Context
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import de.tebbeubben.remora.lib.R
import de.tebbeubben.remora.lib.RemoraLib
import de.tebbeubben.remora.lib.RemoraNotificationChannel
import de.tebbeubben.remora.lib.persistence.repositories.MessageRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Instant

internal class SendMessageWorker(
    private val appContext: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(appContext, workerParams) {

    @Inject
    lateinit var messageRepository: MessageRepository

    @Inject
    lateinit var messageHandler: MessageHandler

    @Inject
    lateinit var remoraNotificationChannel: RemoraNotificationChannel

    companion object {

        const val UNIQUE_WORK_NAME = "RemoraSendMessageWorker"
        private const val NOTIFICATION_ID = 12695
    }

    init {
        RemoraLib.component?.inject(this) ?: error("RemoraLib not initialized")
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        remoraNotificationChannel.createNotificationChannel()

        val foregroundInfo = createForegroundInfo()
        setForeground(foregroundInfo)

        try {
            while (true) {
                val queueEntry = messageRepository.getNextSendQueueEntry() ?: break
                val remainingTtl = queueEntry.ttl?.milliseconds?.let { ttl ->
                    val queuedAt = Instant.fromEpochMilliseconds(queueEntry.queuedAt)
                    val elapsedTime = Clock.System.now() - queuedAt
                    ttl - elapsedTime
                }
                if (remainingTtl == null || remainingTtl > Duration.ZERO) {
                    messageHandler.sendMessage(queueEntry.peer, queueEntry.message, remainingTtl)
                }
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

    private fun createForegroundInfo(): ForegroundInfo {
        val title = appContext.getString(R.string.remoraSending_messages)
        val message = appContext.getString(R.string.remoraSending_messages_to_follower_devices)

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
}