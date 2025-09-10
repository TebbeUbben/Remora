package de.tebbeubben.remora.lib.persistence.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.math.BigInteger

@Entity(
    tableName = "message_id_counters",
    foreignKeys = [ForeignKey(
        entity = Peer::class,
        parentColumns = ["id"],
        childColumns = ["peer"],
        onDelete = ForeignKey.CASCADE,
        onUpdate = ForeignKey.CASCADE
    )]
)
internal data class MessageIdCounter(
    @PrimaryKey
    var peer: Long,
    var replayCache: Long = 0L,
    var lastIngoingMessageId: Long = 0L,
    var lastOutgoingMessageId: Long = 0L,
    var lastStatusId: Long = 0L
)