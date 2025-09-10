package de.tebbeubben.remora.lib.persistence.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import de.tebbeubben.remora.lib.persistence.entities.MessageIdCounter

@Dao
internal interface MessageIdCounterDao {

    @Query("SELECT * FROM message_id_counters WHERE peer = :peerId")
    suspend fun getByPeerId(peerId: Long): MessageIdCounter?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(messageIdCounter: MessageIdCounter)
}