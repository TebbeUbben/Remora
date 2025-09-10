package de.tebbeubben.remora.lib.persistence

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import de.tebbeubben.remora.lib.persistence.daos.MessageIdCounterDao
import de.tebbeubben.remora.lib.persistence.daos.PeerDao
import de.tebbeubben.remora.lib.persistence.daos.SendQueueDao
import de.tebbeubben.remora.lib.persistence.entities.MessageIdCounter
import de.tebbeubben.remora.lib.persistence.entities.Peer
import de.tebbeubben.remora.lib.persistence.entities.SendQueueEntry

@Database(
    version = 1,
    entities = [Peer::class, MessageIdCounter::class, SendQueueEntry::class]
)
@TypeConverters(BigIntegerTypeConverters::class)
internal abstract class RemoraLibDatabase : RoomDatabase() {
    abstract fun peerDao(): PeerDao

    abstract fun messageIdCounterDao(): MessageIdCounterDao
    
    abstract fun sendQueueDao(): SendQueueDao

    companion object {
        const val DATABASE_NAME = "remora-lib.db"
    }
}
