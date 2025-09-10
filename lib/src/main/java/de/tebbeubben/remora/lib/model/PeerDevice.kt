package de.tebbeubben.remora.lib.model

import java.security.PrivateKey
import java.security.PublicKey

sealed class PeerDevice {

    abstract val id: Long

    data class Stub(
        override val id: Long
    ) : PeerDevice()

    data class Initiating(
        override val id: Long,
        val salt: ByteArray,
        val pairingTopic: String,
        val localPrivateKey: PrivateKey,
        val localPublicKey: PublicKey,
    ) : PeerDevice() {

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Initiating

            if (id != other.id) return false
            if (!salt.contentEquals(other.salt)) return false
            if (pairingTopic != other.pairingTopic) return false
            if (localPrivateKey != other.localPrivateKey) return false
            if (localPublicKey != other.localPublicKey) return false

            return true
        }

        override fun hashCode(): Int {
            var result = id.hashCode()
            result = 31 * result + salt.contentHashCode()
            result = 31 * result + pairingTopic.hashCode()
            result = 31 * result + localPrivateKey.hashCode()
            result = 31 * result + localPublicKey.hashCode()
            return result
        }
    }

    data class Handshaking(
        override val id: Long,
        val followerId: Long,
        val pairingTopic: String,
        val localPublicKey: PublicKey,
        val verificationData: ByteArray,
        val ingoingTopic: String,
        val outgoingTopic: String,
    ) : PeerDevice() {

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Handshaking

            if (id != other.id) return false
            if (pairingTopic != other.pairingTopic) return false
            if (localPublicKey != other.localPublicKey) return false
            if (!verificationData.contentEquals(other.verificationData)) return false
            if (ingoingTopic != other.ingoingTopic) return false
            if (outgoingTopic != other.outgoingTopic) return false

            return true
        }

        override fun hashCode(): Int {
            var result = id.hashCode()
            result = 31 * result + pairingTopic.hashCode()
            result = 31 * result + localPublicKey.hashCode()
            result = 31 * result + verificationData.contentHashCode()
            result = 31 * result + ingoingTopic.hashCode()
            result = 31 * result + outgoingTopic.hashCode()
            return result
        }
    }

    data class Verifying(
        override val id: Long,
        val followerId: Long?,
        val verificationData: ByteArray,
        val ingoingTopic: String,
        val outgoingTopic: String,
        val hasPeerVerified: Boolean,
        var hasLocalVerified: Boolean,
    ) : PeerDevice() {

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Verifying

            if (id != other.id) return false
            if (hasPeerVerified != other.hasPeerVerified) return false
            if (hasLocalVerified != other.hasLocalVerified) return false
            if (!verificationData.contentEquals(other.verificationData)) return false
            if (ingoingTopic != other.ingoingTopic) return false
            if (outgoingTopic != other.outgoingTopic) return false

            return true
        }

        override fun hashCode(): Int {
            var result = id.hashCode()
            result = 31 * result + hasPeerVerified.hashCode()
            result = 31 * result + hasLocalVerified.hashCode()
            result = 31 * result + verificationData.contentHashCode()
            result = 31 * result + ingoingTopic.hashCode()
            result = 31 * result + outgoingTopic.hashCode()
            return result
        }
    }

    sealed class Paired : PeerDevice() {
        abstract val pairedAt: Long
        abstract val deviceName: String?
        abstract val ingoingTopic: String
        abstract val outgoingTopic: String
    }

    data class Follower(
        override val id: Long,
        override val pairedAt: Long,
        override val deviceName: String?,
        override val ingoingTopic: String,
        override val outgoingTopic: String,
    ) : Paired()

    data class Main(
        override val id: Long,
        val ownFollowerId: Long,
        override val pairedAt: Long,
        override val deviceName: String?,
        override val ingoingTopic: String,
        override val outgoingTopic: String,
    ) : Paired()
}