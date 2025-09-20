package de.tebbeubben.remora.lib

import com.google.protobuf.kotlin.toByteString
import de.tebbeubben.remora.lib.persistence.repositories.NetworkConfigurationRepository
import de.tebbeubben.remora.lib.messaging.FcmClient
import de.tebbeubben.remora.lib.messaging.FcmSubscriptionManager
import de.tebbeubben.remora.lib.messaging.MessageHandler
import de.tebbeubben.remora.lib.model.PeerDevice
import de.tebbeubben.remora.lib.persistence.repositories.PeerDeviceRepository
import de.tebbeubben.remora.lib.util.Crypto
import de.tebbeubben.remora.proto.pairingData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.PublicKey
import java.security.SecureRandom
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.io.encoding.Base64

@Singleton
internal class PeerDeviceManager @Inject constructor(
    private val peerDeviceRepository: PeerDeviceRepository,
    private val crypto: Crypto,
    private val fcmSubscriptionManager: FcmSubscriptionManager,
    private val fcmClient: FcmClient,
    private val networkConfigurationRepository: NetworkConfigurationRepository,
    private val messageHandler: MessageHandler
) {

    var isPairedToMain = false
        private set

    suspend fun startup() {
        isPairedToMain = getMainDevice() != null
    }

    suspend fun reset() {
        isPairedToMain = false
    }

    fun getPeerDeviceById(id: Long) = peerDeviceRepository.getPeerDeviceByIdFlow(id)

    suspend fun getPeerDevices() = peerDeviceRepository.getPeerDevices()

    suspend fun getFollowers() = peerDeviceRepository.getPeerDevices().filter { it is PeerDevice.Follower }

    fun getPeerDevicesFlow() = peerDeviceRepository.getPeerDevicesFlow()

    suspend fun getMainDevice() = peerDeviceRepository.getMainDevice()

    suspend fun renameDevice(peerDeviceId: Long, newName: String?) = peerDeviceRepository.renamePeerDevice(peerDeviceId, newName)

    suspend fun initiateFollowerPairing(): Long =
        withContext(Dispatchers.Default) {
            val keyPair = crypto.generateECDHKeyPair()

            val salt = ByteArray(32)
            SecureRandom.getInstanceStrong().nextBytes(salt)

            val pairingTopicBytes = ByteArray(16)
            SecureRandom.getInstanceStrong().nextBytes(pairingTopicBytes)

            val pairingTopic = Base64.UrlSafe
                .withPadding(Base64.PaddingOption.ABSENT)
                .encode(pairingTopicBytes)

            val peerDevice = peerDeviceRepository.insertNewPeerDevice(
                salt,
                pairingTopic,
                keyPair.private,
                keyPair.public
            )

            fcmSubscriptionManager.subscribeToPairingTopic(pairingTopic)

            peerDevice.id
        }

    fun getPairingData(peerDevice: PeerDevice.Initiating): ByteArray {
        val networkConfig = networkConfigurationRepository.config!!
        val pairingData = pairingData {
            version = 1
            projectId = networkConfig.projectId
            privateKeyId = networkConfig.privateKeyId
            privateKey = networkConfig.privateKey.encoded.toByteString()
            tokenUri = networkConfig.tokenUri
            clientEmail = networkConfig.clientEmail
            apiKey = networkConfig.apiKey
            applicationId = networkConfig.applicationId
            gcmSenderId = networkConfig.gcmSenderId
            followerId = peerDevice.id
            salt = peerDevice.salt.toByteString()
            pairingTopic = peerDevice.pairingTopic
            publicKey = peerDevice.localPublicKey.encoded.toByteString()
        }
        return pairingData.toByteString().toByteArray()
    }

    suspend fun startPairingAsFollower(
        followerId: Long,
        salt: ByteArray,
        pairingTopic: String,
        remotePublicKey: PublicKey,
    ) = withContext(Dispatchers.Default) {
        val peerDeviceId = peerDeviceRepository.reserveId()

        val keyPair = crypto.generateECDHKeyPair()
        val secret = crypto.deriveECDHSecret(keyPair.private, remotePublicKey)

        val ingoingKey = crypto.hkdf(secret, salt, "remora/key/main".encodeToByteArray(), 16)
        val outgoingKey = crypto.hkdf(secret, salt, "remora/key/follower".encodeToByteArray(), 16)
        val statusKey = crypto.hkdf(secret, salt, "remora/key/status".encodeToByteArray(), 16)
        val ingoingTopic = Base64.UrlSafe.withPadding(Base64.PaddingOption.ABSENT)
            .encode(crypto.hkdf(secret, salt, "remora/topic/main".encodeToByteArray(), 16))
        val outgoingTopic = Base64.UrlSafe.withPadding(Base64.PaddingOption.ABSENT)
            .encode(crypto.hkdf(secret, salt, "remora/topic/follower".encodeToByteArray(), 16))
        val verificationData =
            crypto.hkdf(secret, salt, "remora/verification_data".encodeToByteArray(), 6)

        crypto.storeIngoingKey(peerDeviceId, ingoingKey)
        crypto.storeOutgoingKey(peerDeviceId, outgoingKey)
        crypto.storeIngoingStatusKey(peerDeviceId, statusKey)

        val peerDevice = PeerDevice.Handshaking(
            id = peerDeviceId,
            followerId = followerId,
            pairingTopic = pairingTopic,
            localPublicKey = keyPair.public,
            verificationData = verificationData,
            ingoingTopic = ingoingTopic,
            outgoingTopic = outgoingTopic
        )

        peerDeviceRepository.updatePeerDevice(peerDevice)

        fcmSubscriptionManager.subscribeToPeerTopic(ingoingTopic)

        peerDevice.id
    }

    suspend fun exchangePublicKeyWithMain(peerDeviceId: Long) {
        val peerDevice =
            peerDeviceRepository.getPeerDeviceById(peerDeviceId) as? PeerDevice.Handshaking
                ?: return
        fcmClient.sendFCM(
            FcmSubscriptionManager.PAIRING_TOPIC_PREFIX + peerDevice.pairingTopic,
            buildMap {
                put(
                    key = "",
                    value = Base64.UrlSafe.withPadding(Base64.PaddingOption.ABSENT)
                        .encode(peerDevice.localPublicKey.encoded)
                )
            }
        )
        val verifying = PeerDevice.Verifying(
            id = peerDevice.id,
            followerId = peerDevice.followerId,
            verificationData = peerDevice.verificationData,
            ingoingTopic = peerDevice.ingoingTopic,
            outgoingTopic = peerDevice.outgoingTopic,
            hasPeerVerified = false,
            hasLocalVerified = false
        )
        peerDeviceRepository.updatePeerDevice(verifying)
    }

    suspend fun handlePublicKeyFromFollower(
        initiating: PeerDevice.Initiating,
        remotePublicKey: PublicKey
    ): PeerDevice.Verifying = withContext(Dispatchers.Default) {
        val secret = crypto.deriveECDHSecret(initiating.localPrivateKey, remotePublicKey)

        val outgoingKey =
            crypto.hkdf(secret, initiating.salt, "remora/key/main".encodeToByteArray(), 16)
        val ingoingKey =
            crypto.hkdf(secret, initiating.salt, "remora/key/follower".encodeToByteArray(), 16)
        val statusKey = crypto.hkdf(secret, initiating.salt, "remora/key/status".encodeToByteArray(), 16)
        val outgoingTopic = Base64.UrlSafe.withPadding(Base64.PaddingOption.ABSENT)
            .encode(
                crypto.hkdf(
                    secret,
                    initiating.salt,
                    "remora/topic/main".encodeToByteArray(),
                    16
                )
            )
        val ingoingTopic = Base64.UrlSafe.withPadding(Base64.PaddingOption.ABSENT)
            .encode(
                crypto.hkdf(
                    secret,
                    initiating.salt,
                    "remora/topic/follower".encodeToByteArray(),
                    16
                )
            )
        val verificationData =
            crypto.hkdf(secret, initiating.salt, "remora/verification_data".encodeToByteArray(), 6)

        crypto.storeIngoingKey(initiating.id, ingoingKey)
        crypto.storeOutgoingKey(initiating.id, outgoingKey)
        crypto.storeOutgoingStatusKey(initiating.id, statusKey)

        val verifying = PeerDevice.Verifying(
            id = initiating.id,
            followerId = null,
            verificationData = verificationData,
            ingoingTopic = ingoingTopic,
            outgoingTopic = outgoingTopic,
            hasPeerVerified = false,
            hasLocalVerified = false
        )

        peerDeviceRepository.updatePeerDevice(verifying)

        fcmSubscriptionManager.unsubscribeFromPairingTopic(initiating.pairingTopic)
        fcmSubscriptionManager.subscribeToPeerTopic(ingoingTopic)

        verifying
    }

    suspend fun verifyPairing(
        peerDeviceId: Long
    ) {
        messageHandler.sendVerifyMessage(peerDeviceId)
        isPairedToMain = peerDeviceRepository.updateVerificationState(
            peerDeviceId,
            locallyVerified = true,
            remotelyVerified = false
        )
    }

    suspend fun delete(peerDeviceId: Long) {
        val peer = peerDeviceRepository.getPeerDeviceById(peerDeviceId)
        when (peer) {
            is PeerDevice.Initiating -> fcmSubscriptionManager.unsubscribeFromPairingTopic(peer.pairingTopic)
            is PeerDevice.Handshaking -> fcmSubscriptionManager.unsubscribeFromPeerTopic(peer.ingoingTopic)
            is PeerDevice.Paired -> fcmSubscriptionManager.unsubscribeFromPairingTopic(peer.ingoingTopic)
            is PeerDevice.Verifying -> fcmSubscriptionManager.unsubscribeFromPairingTopic(peer.ingoingTopic)
            else -> Unit
        }
        peerDeviceRepository.deletePeerDeviceById(peerDeviceId)
    }
}