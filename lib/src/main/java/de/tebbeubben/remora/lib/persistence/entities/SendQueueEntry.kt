package de.tebbeubben.remora.lib.persistence.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "send_queue",
    primaryKeys = ["peer", "messageId"],
    indices = [Index(value = ["peer", "collapseKey"], unique = true)],
    foreignKeys = [ForeignKey(
        entity = Peer::class,
        parentColumns = ["id"],
        childColumns = ["peer"],
        onDelete = ForeignKey.CASCADE,
        onUpdate = ForeignKey.CASCADE
    )]
)
internal data class SendQueueEntry(
    var peer: Long,
    var collapseKey: String?,
    var messageId: Long,
    var queuedAt: Long,
    var ttl: Long,
    var message: ByteArray
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SendQueueEntry

        if (peer != other.peer) return false
        if (messageId != other.messageId) return false
        if (queuedAt != other.queuedAt) return false
        if (ttl != other.ttl) return false
        if (collapseKey != other.collapseKey) return false
        if (!message.contentEquals(other.message)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = peer.hashCode()
        result = 31 * result + messageId.hashCode()
        result = 31 * result + queuedAt.hashCode()
        result = 31 * result + ttl.hashCode()
        result = 31 * result + collapseKey.hashCode()
        result = 31 * result + message.contentHashCode()
        return result
    }
}