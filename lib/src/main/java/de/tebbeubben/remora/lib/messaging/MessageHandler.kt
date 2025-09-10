package de.tebbeubben.remora.lib.messaging

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.google.protobuf.InvalidProtocolBufferException
import dagger.Lazy
import de.tebbeubben.remora.lib.PeerDeviceManager
import de.tebbeubben.remora.lib.di.ApplicationContext
import de.tebbeubben.remora.lib.model.Message
import de.tebbeubben.remora.lib.model.PeerDevice
import de.tebbeubben.remora.lib.persistence.repositories.MessageRepository
import de.tebbeubben.remora.lib.persistence.repositories.PeerDeviceRepository
import de.tebbeubben.remora.lib.status.StatusManager
import de.tebbeubben.remora.lib.util.Crypto
import de.tebbeubben.remora.proto.MessageWrapper
import de.tebbeubben.remora.proto.messageWrapper
import de.tebbeubben.remora.proto.messages.StatusMessage
import de.tebbeubben.remora.proto.messages.verifyMessage
import kotlinx.coroutines.guava.await
import java.security.InvalidKeyException
import java.security.spec.InvalidKeySpecException
import javax.crypto.AEADBadTagException
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.io.encoding.Base64

@Singleton
internal class MessageHandler @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val crypto: Crypto,
    private val messageRepository: MessageRepository,
    private val fcmClient: FcmClient,
    private val peerDeviceRepository: PeerDeviceRepository,
    private val peerDeviceManager: Lazy<PeerDeviceManager>,
    private val statusManager: StatusManager,
) {

    suspend fun sendVerifyMessage(peer: Long) {
        sendMessage(peer, messageWrapper { verify = verifyMessage { } })
    }

    private suspend fun prepareMessage(peer: Long, payload: ByteArray): Message {
        val nextMessageId = messageRepository.getNextOutgoingMessageId(peer)
        if (payload.size > 3000) error("Maximum payload size exceeded.")
        val header = ByteArray(5)
        header[0] = PROTOCOL_VERSION
        header[1] = (nextMessageId ushr 24).toByte()
        header[2] = (nextMessageId ushr 16).toByte()
        header[3] = (nextMessageId ushr 8).toByte()
        header[4] = nextMessageId.toByte()
        val iv = ByteArray(12)
        iv[8] = (nextMessageId ushr 24).toByte()
        iv[9] = (nextMessageId ushr 16).toByte()
        iv[10] = (nextMessageId ushr 8).toByte()
        iv[11] = nextMessageId.toByte()
        val encrypted = crypto.encryptMessageAESGCM(peer, iv, payload, header)
        header + encrypted
        return Message(
            peer = peer,
            messageId = nextMessageId,
            payload = header + encrypted
        )
    }

    suspend fun sendStatusMessage(peer: Long, statusMessage: StatusMessage) {
        sendMessage(peer, messageWrapper { status = statusMessage }, collapseKey = "status")
    }

    private suspend fun sendMessage(peer: Long, wrapper: MessageWrapper, collapseKey: String? = null) {
        val message = prepareMessage(peer, wrapper.toByteArray())
        sendMessage(peer, message.payload, collapseKey)
    }

    suspend fun enqueueMessage(peer: Long, wrapper: MessageWrapper, collapseKey: String? = null) {
        val message = prepareMessage(peer, wrapper.toByteArray())
        messageRepository.addToQueue(message, collapseKey)
        ensureSendMessageWorkerIsScheduled()
    }
    private suspend fun ensureSendMessageWorkerIsScheduled() {
        val workManager = WorkManager.getInstance(context)
        val workInfos: List<WorkInfo>? =
            workManager.getWorkInfosForUniqueWork(SendMessageWorker.UNIQUE_WORK_NAME).await()
        val workInfo = workInfos?.lastOrNull()
        val isRunning = when (workInfo?.state) {
            WorkInfo.State.RUNNING  -> true
            WorkInfo.State.ENQUEUED -> workInfo.runAttemptCount == 0
            else                    -> false
        }

        if (isRunning) return

        workManager.enqueueUniqueWork(
            SendMessageWorker.UNIQUE_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            OneTimeWorkRequestBuilder<SendMessageWorker>()
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .setConstraints(
                    Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
                )
                .build()

        )

    }

    suspend fun sendMessage(peer: Long, payload: ByteArray, collapseKey: String? = null) {
        val peerDevice = peerDeviceRepository.getPeerDeviceById(peer) ?: return
        val topic = when (peerDevice) {
            is PeerDevice.Paired    -> peerDevice.outgoingTopic
            is PeerDevice.Verifying -> peerDevice.outgoingTopic
            else                    -> return
        }
        fcmClient.sendFCM(
            topic = FcmSubscriptionManager.PEER_TOPIC_PREFIX + topic,
            data = buildMap {
                put("", Base64.UrlSafe.withPadding(Base64.PaddingOption.ABSENT).encode(payload))
            },
            collapseKey
        )
    }

    private suspend fun handleMessage(peerDevice: PeerDevice, message: ByteArray) {
        if (message.size < 21) {
            //TODO: Log
            return
        }
        val version = message[0]

        if (version != PROTOCOL_VERSION) {
            //TODO: Log
            return
        }

        val messageId = (message[1].toLong() and 0xFF shl 24) or
            (message[2].toLong() and 0xFF shl 16) or
            (message[3].toLong() and 0xFF shl 8) or
            (message[4].toLong() and 0xFF)

        val header = message.copyOf(5)

        val iv = ByteArray(12)
        iv[8] = (messageId ushr 24).toByte()
        iv[9] = (messageId ushr 16).toByte()
        iv[10] = (messageId ushr 8).toByte()
        iv[11] = messageId.toByte()

        val data = message.copyOfRange(5, message.size)

        val decrypted = try {
            crypto.decryptMessageAESGCM(peerDevice.id, iv, data, header)
        } catch (e: AEADBadTagException) {
            //TODO: Log
            return
        }

        if (!messageRepository.updateLastIngoingMessageId(peerDevice.id, messageId)) {
            //TODO: Log
            return
        }

        handleMessage(peerDevice.id, messageId, decrypted)
    }

    private suspend fun handleMessage(peerDeviceId: Long, messageId: Long, message: ByteArray) {
        try {
            val wrapper = MessageWrapper.parseFrom(message)
            when (wrapper.messageCase) {
                MessageWrapper.MessageCase.VERIFY -> {
                    peerDeviceRepository.updateVerificationState(
                        peerDeviceId = peerDeviceId,
                        locallyVerified = false,
                        remotelyVerified = true
                    )
                }

                MessageWrapper.MessageCase.STATUS -> {
                    val statusMessage = wrapper.status
                    statusManager.onReceiveShortStatus(statusMessage.statusId.toLong() and 0xFFFFFFFF, statusMessage.shortStatusData)
                }

                else                              -> Unit
            }
        } catch (e: InvalidProtocolBufferException) {
            //TODO: Log
        }
    }

    suspend fun onReceiveData(from: String, data: ByteArray) {
        when {
            from.startsWith("/topics/${FcmSubscriptionManager.PAIRING_TOPIC_PREFIX}") -> {
                val pairingTopic =
                    from.substring(FcmSubscriptionManager.PAIRING_TOPIC_PREFIX.length + 8)
                val peerDevice =
                    peerDeviceRepository.getPeerDeviceByPairingTopic(pairingTopic) as? PeerDevice.Initiating
                if (peerDevice == null) {
                    //TODO: Log
                    return
                }
                try {
                    val publicKey = crypto.decodeECDHPublicKey(data)
                    peerDeviceManager.get().handlePublicKeyFromFollower(peerDevice, publicKey)
                } catch (e: InvalidKeyException) {
                    //TODO: Log
                } catch (e: InvalidKeySpecException) {
                    //TODO: Log
                }
            }

            from.startsWith("/topics/${FcmSubscriptionManager.PEER_TOPIC_PREFIX}")    -> {
                val peerTopic = from.substring(FcmSubscriptionManager.PEER_TOPIC_PREFIX.length + 8)
                val peerDevice = peerDeviceRepository.getPeerDeviceByIngoingTopic(peerTopic)
                if (peerDevice == null) {
                    //TODO: Log
                    return
                }
                handleMessage(peerDevice, data)
            }
        }
    }

    companion object {

        const val PROTOCOL_VERSION = 0x01.toByte()
    }
}