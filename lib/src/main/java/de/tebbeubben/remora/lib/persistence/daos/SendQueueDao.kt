package de.tebbeubben.remora.lib.persistence.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import de.tebbeubben.remora.lib.persistence.entities.SendQueueEntry

@Dao
internal interface SendQueueDao {

    @Insert
    suspend fun insert(entries: SendQueueEntry)

    @Delete
    suspend fun delete(entry: SendQueueEntry)

    @Query("SELECT * FROM send_queue ORDER BY queuedAt ASC LIMIT 1")
    suspend fun getNext(): SendQueueEntry?
}
