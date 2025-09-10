package de.tebbeubben.remora.lib.persistence.repositories

import androidx.room.withTransaction
import de.tebbeubben.remora.lib.model.Message
import de.tebbeubben.remora.lib.persistence.RemoraLibDatabase
import de.tebbeubben.remora.lib.persistence.daos.MessageIdCounterDao
import de.tebbeubben.remora.lib.persistence.daos.SendQueueDao
import de.tebbeubben.remora.lib.persistence.entities.MessageIdCounter
import de.tebbeubben.remora.lib.persistence.entities.SendQueueEntry
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class MessageRepository @Inject constructor(
    private val database: RemoraLibDatabase,
    private val messageIdCounterDao: MessageIdCounterDao,
    private val sendQueueDao: SendQueueDao
) {

    suspend fun getNextStatusId(peerId: Long): Long = database.withTransaction {
        val messageIdCounter = messageIdCounterDao.getByPeerId(peerId) ?: MessageIdCounter(peerId)
        messageIdCounter.lastStatusId += 1
        messageIdCounterDao.insertOrUpdate(messageIdCounter)
        messageIdCounter.lastStatusId
    }

    suspend fun getNextOutgoingMessageId(peerId: Long): Long = database.withTransaction {
        val messageIdCounter = messageIdCounterDao.getByPeerId(peerId) ?: MessageIdCounter(peerId)
        messageIdCounter.lastOutgoingMessageId += 1
        messageIdCounterDao.insertOrUpdate(messageIdCounter)
        messageIdCounter.lastOutgoingMessageId
    }

    suspend fun updateLastStatusId(peerId: Long, statusId: Long) = database.withTransaction {
        val messageIdCounter = messageIdCounterDao.getByPeerId(peerId) ?: MessageIdCounter(peerId)
        if (statusId > messageIdCounter.lastStatusId) {
            messageIdCounter.lastStatusId = statusId
            messageIdCounterDao.insertOrUpdate(messageIdCounter)
            return@withTransaction true
        }
        false
    }

    suspend fun updateLastIngoingMessageId(peerId: Long, messageId: Long) = database.withTransaction {
        val messageIdCounter = messageIdCounterDao.getByPeerId(peerId) ?: MessageIdCounter(peerId)

        if (messageId > messageIdCounter.lastIngoingMessageId) {
            val shiftAmount = (messageId - messageIdCounter.lastIngoingMessageId).toInt()
            if (shiftAmount < Long.SIZE_BITS) { // Ensure shift is within Long's bit size
                messageIdCounter.replayCache = messageIdCounter.replayCache shl shiftAmount
            } else {
                messageIdCounter.replayCache = 0L // Effectively shifting everything out
            }
            // Set the bit for the new messageId
            messageIdCounter.replayCache = messageIdCounter.replayCache or (1L shl 0) // The newest messageId is at the 0th bit position after shifting
            messageIdCounter.lastIngoingMessageId = messageId
            messageIdCounterDao.insertOrUpdate(messageIdCounter)
            return@withTransaction true
        } else {
            // messageId is not larger, check replay cache
            val bitPosition = (messageIdCounter.lastIngoingMessageId - messageId).toInt()

            // Check if the messageId is within the replay cache window
            if (bitPosition < 0 || bitPosition >= Long.SIZE_BITS) {
                // Message is too old or too new (should have been caught by the first `if`)
                // and outside the effective replay cache size
                return@withTransaction false
            }

            val mask = 1L shl bitPosition
            if ((messageIdCounter.replayCache and mask) == 0L) {
                // Bit is 0, update to 1
                messageIdCounter.replayCache = messageIdCounter.replayCache or mask
                messageIdCounterDao.insertOrUpdate(messageIdCounter)
                return@withTransaction true
            } else {
                // Bit is already 1 (potential replay)
                return@withTransaction false
            }
        }
    }

    suspend fun addToQueue(message: Message, collapseKey: String?) = database.withTransaction {
        if (collapseKey != null) {
            sendQueueDao.deleteWithCollapseKey(message.peer, collapseKey)
        }
        sendQueueDao.insert(
            SendQueueEntry(
                peer = message.peer,
                collapseKey = collapseKey,
                messageId = message.messageId,
                queuedAt = System.currentTimeMillis(),
                ttl = 0, // TODO
                message = message.payload
            )
        )
    }

    suspend fun getNextSendQueueEntry() = sendQueueDao.getNext()

    suspend fun delete(sendQueueEntry: SendQueueEntry) = sendQueueDao.delete(sendQueueEntry)
}