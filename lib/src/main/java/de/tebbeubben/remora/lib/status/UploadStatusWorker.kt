package de.tebbeubben.remora.lib.status

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.google.protobuf.kotlin.toByteString
import de.tebbeubben.remora.lib.PeerDeviceManager
import de.tebbeubben.remora.lib.R
import de.tebbeubben.remora.lib.RemoraLib
import de.tebbeubben.remora.lib.messaging.MessageHandler
import de.tebbeubben.remora.lib.persistence.repositories.MessageRepository
import de.tebbeubben.remora.lib.status.StatusManager.Companion.STATUS_VERSION
import de.tebbeubben.remora.lib.util.Crypto
import de.tebbeubben.remora.proto.StatusData
import de.tebbeubben.remora.proto.messages.statusMessage
import de.tebbeubben.remora.proto.statusEnvelope
import de.tebbeubben.remora.proto.wrappedKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okio.IOException
import java.io.File
import java.security.SecureRandom
import javax.inject.Inject

internal class UploadStatusWorker(
    private val appContext: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(appContext, workerParams) {

    companion object {

        const val UNIQUE_WORK_NAME = "UploadStatusWorker"
        private const val NOTIFICATION_ID = 12696
        private const val CHANNEL_ID = "RemoraUploadStatusChannel"
    }

    @Inject
    lateinit var messageHandler: MessageHandler

    @Inject
    lateinit var messageIdRepository: MessageRepository

    @Inject
    lateinit var crypto: Crypto

    @Inject
    lateinit var statusManager: StatusManager

    @Inject
    lateinit var peerDeviceManager: PeerDeviceManager

    init {
        RemoraLib.component?.inject(this) ?: error("RemoraLib not initialized")
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        createNotificationChannel()

        val foregroundInfo = createForegroundInfo()
        setForeground(foregroundInfo)

        val dataPath = inputData.getString("data_path")!!

        val dataFile = File(dataPath)
        val data = dataFile.readBytes()
        val statusData = StatusData.parseFrom(data)

        val statusKey = ByteArray(16)
        SecureRandom().nextBytes(statusKey)

        val encryptedStatus = crypto.encryptAESCTR(
            key = statusKey,
            iv = ByteArray(12),
            data = data
        )

        val followers = peerDeviceManager.getFollowers()

        if (followers.isEmpty()) {
            // Not uploading status since there's not follower to see it anyways.
            return@withContext Result.success()
            //TODO: Log
        }

        val wrappedKeys = followers.map { follower ->
            wrappedKey {
                this.followerId = follower.id
                val statusId = messageIdRepository.getNextStatusId(follower.id)
                this.statusId = statusId.toInt()
                val iv = ByteArray(12)
                iv[8] = (statusId ushr 24).toByte()
                iv[9] = (statusId ushr 16).toByte()
                iv[10] = (statusId ushr 8).toByte()
                iv[11] = statusId.toByte()
                this.encryptedStatusKey = crypto.encryptStatusKeyAESGCM(
                    peerDeviceId = follower.id,
                    iv = iv,
                    data = statusKey,
                    aad = byteArrayOf(STATUS_VERSION.toByte()) + encryptedStatus
                ).toByteString()
            }
        }

        val statusEnvelope = statusEnvelope {
            this.version = STATUS_VERSION
            this.encryptedStatusData = encryptedStatus.toByteString()
            this.wrappedKeys += wrappedKeys
        }

        statusManager.uploadEncryptedStatus(statusEnvelope)

        dataFile.delete()

        coroutineScope {
            for (wrappedKey in wrappedKeys) {
                val statusMessage = statusMessage {
                    this.statusId = wrappedKey.statusId
                    this.shortStatusData = statusData.shortStatusData
                }
                launch {
                    try {
                        messageHandler.sendStatusMessage(wrappedKey.followerId, statusMessage)
                    } catch (e: IOException) {
                        //TODO: Log
                    }
                }
            }
        }

        Result.success()
    }

    private fun createNotificationChannel() {
        val name = "Status Uploading Service"
        val descriptionText = "Handles uploading of status to remote devices"
        val importance = NotificationManager.IMPORTANCE_LOW
        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }
        val notificationManager: NotificationManager =
            appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    override suspend fun getForegroundInfo(): ForegroundInfo = createForegroundInfo()

    private fun createForegroundInfo(): ForegroundInfo {
        val title = "Uploading Status"
        val message = "Uploading status to remote devices..."

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