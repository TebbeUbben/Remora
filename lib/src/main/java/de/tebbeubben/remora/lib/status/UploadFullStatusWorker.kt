package de.tebbeubben.remora.lib.status

import android.content.Context
import androidx.work.WorkerParameters
import com.google.protobuf.kotlin.toByteString
import de.tebbeubben.remora.lib.RemoraLib
import de.tebbeubben.remora.lib.messaging.MessageHandler
import de.tebbeubben.remora.lib.status.StatusManager.Companion.STATUS_VERSION
import de.tebbeubben.remora.lib.util.Crypto
import de.tebbeubben.remora.proto.StatusData
import de.tebbeubben.remora.proto.messages.statusMessage
import de.tebbeubben.remora.proto.statusEnvelope
import de.tebbeubben.remora.proto.wrappedKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.security.SecureRandom
import javax.inject.Inject

internal class UploadFullStatusWorker(
    private val appContext: Context,
    workerParams: WorkerParameters,
) : AbstractStatusWorker(appContext, workerParams) {

    @Inject
    lateinit var statusManager: StatusManager

    @Inject
    lateinit var crypto: Crypto

    @Inject
    lateinit var messageHandler: MessageHandler

    init {
        RemoraLib.component?.inject(this) ?: error("RemoraLib not initialized")
    }

    override suspend fun doWorkImpl(): Result = withContext(Dispatchers.Default) {
        val dataPath = inputData.getString("data_path") ?: error("data_path must be provided.")

        val statusIds = inputData.keyValueMap
            .filter { it.key.startsWith("follower_") }
            .map { it.key.removePrefix("follower_").toLong() to it.value as Long }


        val dataFile = File(dataPath)
        val data = dataFile.readBytes()

        val statusKey = ByteArray(16)
        SecureRandom().nextBytes(statusKey)

        val encryptedStatus = crypto.encryptAESCTR(
            key = statusKey,
            iv = ByteArray(12),
            data = data
        )

        val wrappedKeys = statusIds.map { (followerId, statusId) ->
            wrappedKey {
                this.followerId = followerId
                val statusId = statusId
                this.statusId = statusId.toInt()
                val iv = ByteArray(12)
                iv[8] = (statusId ushr 24).toByte()
                iv[9] = (statusId ushr 16).toByte()
                iv[10] = (statusId ushr 8).toByte()
                iv[11] = statusId.toByte()
                this.encryptedStatusKey = crypto.encryptStatusKeyAESGCM(
                    peerDeviceId = followerId,
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

        Result.success()
    }

}