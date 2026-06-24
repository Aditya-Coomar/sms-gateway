package com.aditya.simgateway.data.repository

import com.aditya.simgateway.core.common.CompactIdGenerator
import com.aditya.simgateway.data.dao.MessageDao
import com.aditya.simgateway.data.entity.MessageEntity
import com.aditya.simgateway.data.entity.MessageStatus
import kotlinx.coroutines.flow.Flow

class MessageRepository(
    private val messageDao: MessageDao
) {

    suspend fun createMessage(
        recipient: String,
        body: String,
        simSlot: Int,
        status: MessageStatus = MessageStatus.CREATED
    ): MessageEntity {
        val messageId = CompactIdGenerator.newId(prefix = "msg")
        val message = MessageEntity(
            id = messageId,
            recipient = recipient,
            body = body,
            simSlot = simSlot,
            status = status,
            retryCount = 0,
            createdAt = System.currentTimeMillis(),
            sentAt = null,
            deliveredAt = null,
            failureReason = null
        )
        messageDao.insertMessage(message)
        trimOldMessages()
        return message
    }

    suspend fun upsertMessage(message: MessageEntity) {
        messageDao.insertMessage(message)
    }

    suspend fun getMessageById(id: String): MessageEntity? = messageDao.getById(id)

    fun observeMessageById(id: String): Flow<MessageEntity?> = messageDao.observeById(id)

    fun fetchMessageHistory(): Flow<List<MessageEntity>> = messageDao.getAll()

    suspend fun trimOldMessages() {
        messageDao.deleteOld(retainCount = MESSAGE_RETENTION_LIMIT)
    }

    companion object {
        private const val MESSAGE_RETENTION_LIMIT = 50_000
    }
}
