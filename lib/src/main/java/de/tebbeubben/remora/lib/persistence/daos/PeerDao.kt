package de.tebbeubben.remora.lib.persistence.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import de.tebbeubben.remora.lib.persistence.entities.Peer
import kotlinx.coroutines.flow.Flow

@Dao
internal interface PeerDao {

    @Query("SELECT * FROM peers")
    suspend fun getAll(): List<Peer>

    @Query("SELECT * FROM peers WHERE id = :id")
    suspend fun getById(id: Long): Peer?

    @Query("SELECT * FROM peers WHERE pairingTopic = :pairingTopic")
    suspend fun getByPairingTopic(pairingTopic: String): Peer?

    @Query("SELECT * FROM peers WHERE ingoingTopic = :ingoingTopic")
    suspend fun getByIngoingTopic(ingoingTopic: String): Peer?

    @Query("SELECT * FROM peers WHERE pairingStage = 'PAIRED' LIMIT 1")
    suspend fun getMainDevice(): Peer?

    @Query("SELECT * FROM peers WHERE id = :id")
    fun getByIdFlow(id: Long): Flow<List<Peer>>

    @Query("SELECT * FROM peers")
    fun getAllFlow(): Flow<List<Peer>>

    @Query("UPDATE peers SET deviceName = :deviceName WHERE id = :id")
    suspend fun setDeviceName(id: Long, deviceName: String?)


    @Update
    suspend fun update(peer: Peer)

    @Insert
    suspend fun insert(peer: Peer): Long

    @Query("DELETE FROM peers WHERE id = :id")
    suspend fun deleteById(id: Long)
}