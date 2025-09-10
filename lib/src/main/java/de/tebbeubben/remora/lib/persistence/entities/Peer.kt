package de.tebbeubben.remora.lib.persistence.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.math.BigInteger

@Entity(
    tableName = "peers",
    indices = [
        Index("ingoingTopic", unique = true),
        Index("outgoingTopic", unique = true)
    ]
)
internal data class Peer(
    @PrimaryKey
    var id: Long? = null,

    var followerId: Long? = null,

    var pairingStage: PairingStage = PairingStage.STUB,
    var pairedAt: Long? = null,
    var deviceName: String? = null,

    var salt: ByteArray? = null,
    var pairingTopic: String? = null,
    var localPrivateKey: ByteArray? = null,
    var localPublicKey: ByteArray? = null,
    var peerPublicKey: ByteArray? = null,
    var verificationData: ByteArray? = null,

    var hasPeerVerified: Boolean = false,
    var hasLocalVerified: Boolean = false,

    var ingoingTopic: String? = null,
    var outgoingTopic: String? = null
) {

    enum class PairingStage {
        /**
         * Used to obtain a new auto-increment ID.
         */
        STUB,

        /**
         * We are initiating the pairing and are waiting for the peer
         * to submit their public key on the pairing topic.
         */
        INITIATING,

        /**
         * We have received the initial pairing data and are transmitting
         * our own public key via the pairing topic.
         */
        HANDSHAKING,

        /**
         * The key exchange was successful and we are waiting for the use
         * to verify the pairing data.
         */
        VERIFYING,

        /**
         * The peer device is successfully paired and verified.
         */
        PAIRED
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Peer

        if (id != other.id) return false
        if (hasPeerVerified != other.hasPeerVerified) return false
        if (hasLocalVerified != other.hasLocalVerified) return false
        if (pairingStage != other.pairingStage) return false
        if (!salt.contentEquals(other.salt)) return false
        if (pairingTopic != other.pairingTopic) return false
        if (!localPrivateKey.contentEquals(other.localPrivateKey)) return false
        if (!localPublicKey.contentEquals(other.localPublicKey)) return false
        if (!peerPublicKey.contentEquals(other.peerPublicKey)) return false
        if (!verificationData.contentEquals(other.verificationData)) return false
        if (ingoingTopic != other.ingoingTopic) return false
        if (outgoingTopic != other.outgoingTopic) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id?.hashCode() ?: 0
        result = 31 * result + hasPeerVerified.hashCode()
        result = 31 * result + hasLocalVerified.hashCode()
        result = 31 * result + pairingStage.hashCode()
        result = 31 * result + (salt?.contentHashCode() ?: 0)
        result = 31 * result + (pairingTopic?.hashCode() ?: 0)
        result = 31 * result + (localPrivateKey?.contentHashCode() ?: 0)
        result = 31 * result + (localPublicKey?.contentHashCode() ?: 0)
        result = 31 * result + (peerPublicKey?.contentHashCode() ?: 0)
        result = 31 * result + (verificationData?.contentHashCode() ?: 0)
        result = 31 * result + (ingoingTopic?.hashCode() ?: 0)
        result = 31 * result + (outgoingTopic?.hashCode() ?: 0)
        return result
    }
}