package de.tebbeubben.remora.lib.persistence.repositories

import android.util.Log
import androidx.room.withTransaction
import de.tebbeubben.remora.lib.util.Crypto
import de.tebbeubben.remora.lib.messaging.FcmSubscriptionManager
import de.tebbeubben.remora.lib.model.PeerDevice
import de.tebbeubben.remora.lib.persistence.RemoraLibDatabase
import de.tebbeubben.remora.lib.persistence.daos.PeerDao
import de.tebbeubben.remora.lib.persistence.entities.Peer
import kotlinx.coroutines.flow.map
import java.security.PrivateKey
import java.security.PublicKey
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class PeerDeviceRepository @Inject constructor(
    private val peerDao: PeerDao,
    private val crypto: Crypto,
    private val database: RemoraLibDatabase
) {

    suspend fun deletePeerDeviceById(id: Long) = peerDao.deleteById(id)

    suspend fun getPeerDeviceById(id: Long) = peerDao.getById(id)?.let(::convert)

    suspend fun getMainDevice() = peerDao.getMainDevice()?.let(::convert)?.let { it as? PeerDevice.Main }

    fun getPeerDeviceByIdFlow(id: Long) =
        peerDao.getByIdFlow(id).map { it.firstOrNull()?.let(::convert) }

    fun getPeerDevicesFlow() = peerDao.getAllFlow().map { it.map(::convert) }

    suspend fun getPeerDevices() = peerDao.getAll().map(::convert)

    suspend fun getPeerDeviceByIngoingTopic(ingoingTopic: String) =
        peerDao.getByIngoingTopic(ingoingTopic)?.let(::convert)


    suspend fun getPeerDeviceByPairingTopic(ingoingTopic: String) =
        peerDao.getByPairingTopic(ingoingTopic)?.let(::convert)

    suspend fun updatePeerDevice(peerDevice: PeerDevice) =
        peerDao.update(convert(peerDevice))

    suspend fun renamePeerDevice(peerDeviceId: Long, newName: String?) = peerDao.setDeviceName(peerDeviceId, newName)

    suspend fun insertNewPeerDevice(
        salt: ByteArray,
        pairingTopic: String,
        privateKey: PrivateKey,
        publicKey: PublicKey
    ): PeerDevice.Initiating {
        val id = peerDao.insert(
            Peer(
                pairingStage = Peer.PairingStage.INITIATING,
                salt = salt,
                pairingTopic = pairingTopic,
                localPrivateKey = crypto.encryptECDHPrivateKey(privateKey),
                localPublicKey = publicKey.encoded
            )
        )
        return PeerDevice.Initiating(
            id = id,
            salt = salt,
            pairingTopic = pairingTopic,
            localPrivateKey = privateKey,
            localPublicKey = publicKey
        )
    }

    suspend fun reserveId() = peerDao.insert(Peer(pairingStage = Peer.PairingStage.STUB))

    suspend fun getAllSubscribeTopics(): List<String> {
        val peers = peerDao.getAll()
        val pairingTopics = peers.filter { it.pairingStage == Peer.PairingStage.INITIATING }
            .map { FcmSubscriptionManager.PAIRING_TOPIC_PREFIX + it.pairingTopic }
        val peerTopics = peers.map { FcmSubscriptionManager.PEER_TOPIC_PREFIX + it.ingoingTopic }
        return pairingTopics + peerTopics
    }

    suspend fun updateVerificationState(
        peerDeviceId: Long,
        locallyVerified: Boolean,
        remotelyVerified: Boolean
    ): Boolean = database.withTransaction {
        val verifying = getPeerDeviceById(peerDeviceId) as? PeerDevice.Verifying ?: return@withTransaction false
        Log.d("Verifying", verifying.toString())
        if ((verifying.hasPeerVerified || remotelyVerified) &&
            (verifying.hasLocalVerified || locallyVerified)
        ) {
            val paired = if (verifying.followerId != null) {
                PeerDevice.Main(
                    id = verifying.id,
                    ownFollowerId = verifying.followerId,
                    deviceName = null,
                    pairedAt = System.currentTimeMillis(),
                    ingoingTopic = verifying.ingoingTopic,
                    outgoingTopic = verifying.outgoingTopic
                )
            } else {
                PeerDevice.Follower(
                    id = verifying.id,
                    deviceName = null,
                    pairedAt = System.currentTimeMillis(),
                    ingoingTopic = verifying.ingoingTopic,
                    outgoingTopic = verifying.outgoingTopic
                )
            }
            updatePeerDevice(paired)
            true
        } else {
            val verified = verifying.copy(
                hasPeerVerified = verifying.hasPeerVerified || remotelyVerified,
                hasLocalVerified = verifying.hasLocalVerified || locallyVerified
            )
            updatePeerDevice(verified)
            false
        }
    }

    private fun convert(peer: Peer): PeerDevice = when (peer.pairingStage) {
        Peer.PairingStage.STUB -> PeerDevice.Stub(peer.id!!)
        Peer.PairingStage.INITIATING -> PeerDevice.Initiating(
            id = peer.id!!,
            salt = peer.salt!!,
            pairingTopic = peer.pairingTopic!!,
            localPrivateKey = crypto.decryptECDHPrivateKey(peer.localPrivateKey!!),
            localPublicKey = crypto.decodeECDHPublicKey(peer.localPublicKey!!)
        )

        Peer.PairingStage.HANDSHAKING -> PeerDevice.Handshaking(
            id = peer.id!!,
            followerId = peer.followerId!!,
            pairingTopic = peer.pairingTopic!!,
            localPublicKey = crypto.decodeECDHPublicKey(peer.localPublicKey!!),
            verificationData = peer.verificationData!!,
            ingoingTopic = peer.ingoingTopic!!,
            outgoingTopic = peer.outgoingTopic!!
        )

        Peer.PairingStage.VERIFYING -> PeerDevice.Verifying(
            id = peer.id!!,
            followerId = peer.followerId,
            verificationData = peer.verificationData!!,
            ingoingTopic = peer.ingoingTopic!!,
            outgoingTopic = peer.outgoingTopic!!,
            hasPeerVerified = peer.hasPeerVerified,
            hasLocalVerified = peer.hasLocalVerified
        )

        Peer.PairingStage.PAIRED -> when (peer.followerId) {
            null -> PeerDevice.Follower(
                id = peer.id!!,
                pairedAt = peer.pairedAt!!,
                deviceName = peer.deviceName,
                ingoingTopic = peer.ingoingTopic!!,
                outgoingTopic = peer.outgoingTopic!!
            )
            else -> PeerDevice.Main(
                id = peer.id!!,
                ownFollowerId = peer.followerId!!,
                pairedAt = peer.pairedAt!!,
                deviceName = peer.deviceName,
                ingoingTopic = peer.ingoingTopic!!,
                outgoingTopic = peer.outgoingTopic!!
            )
        }
    }

    private fun convert(peerDevice: PeerDevice): Peer = when (peerDevice) {
        is PeerDevice.Stub -> Peer(
            peerDevice.id,
            pairingStage = Peer.PairingStage.STUB
        )

        is PeerDevice.Initiating -> Peer(
            id = peerDevice.id,
            pairingStage = Peer.PairingStage.INITIATING,
            salt = peerDevice.salt,
            pairingTopic = peerDevice.pairingTopic,
            localPrivateKey = crypto.encryptECDHPrivateKey(peerDevice.localPrivateKey),
            localPublicKey = peerDevice.localPublicKey.encoded
        )

        is PeerDevice.Handshaking -> Peer(
            id = peerDevice.id,
            followerId = peerDevice.followerId,
            pairingStage = Peer.PairingStage.HANDSHAKING,
            pairingTopic = peerDevice.pairingTopic,
            localPublicKey = peerDevice.localPublicKey.encoded,
            verificationData = peerDevice.verificationData,
            ingoingTopic = peerDevice.ingoingTopic,
            outgoingTopic = peerDevice.outgoingTopic
        )

        is PeerDevice.Verifying -> Peer(
            id = peerDevice.id,
            followerId = peerDevice.followerId,
            pairingStage = Peer.PairingStage.VERIFYING,
            verificationData = peerDevice.verificationData,
            ingoingTopic = peerDevice.ingoingTopic,
            outgoingTopic = peerDevice.outgoingTopic,
            hasPeerVerified = peerDevice.hasPeerVerified,
            hasLocalVerified = peerDevice.hasLocalVerified
        )

        is PeerDevice.Paired -> Peer(
            id = peerDevice.id,
            followerId = if (peerDevice is PeerDevice.Main) peerDevice.ownFollowerId else null,
            deviceName = peerDevice.deviceName,
            pairedAt = peerDevice.pairedAt,
            pairingStage = Peer.PairingStage.PAIRED,
            ingoingTopic = peerDevice.ingoingTopic,
            outgoingTopic = peerDevice.outgoingTopic
        )
    }
}