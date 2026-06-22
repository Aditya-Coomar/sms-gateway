package com.aditya.simgateway.data.repository

import com.aditya.simgateway.data.dao.MessageDao
import com.aditya.simgateway.data.entity.MessageEntity
import com.aditya.simgateway.data.entity.MessageStatus
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class MessageRepository(
    private val messageDao: MessageDao
) {

    suspend fun createMessage(
        recipient: String,
        body: String,
        simSlot: Int,
        status: MessageStatus = MessageStatus.QUEUED
    ): String {
        val messageId = UUID.randomUUID().toString()
        messageDao.insertMessage(
            MessageEntity(
                id = messageId,
                recipient = recipient,
                body = body,
                simSlot = simSlot,
                status = status,
                createdAt = System.currentTimeMillis(),
                sentAt = null,
                deliveredAt = null,
                errorMessage = null
            )
        )
        trimOldMessages()
        return messageId
    }

    suspend fun updateMessageStatus(
        id: String,
        status: MessageStatus,
        sentAt: Long? = null,
        deliveredAt: Long? = null,
        errorMessage: String? = null
    ) {
        messageDao.updateStatus(id, status, sentAt, deliveredAt, errorMessage)
    }

    suspend fun getMessageById(id: String): MessageEntity? = messageDao.getById(id)

    fun fetchMessageHistory(): Flow<List<MessageEntity>> = messageDao.getAll()

    suspend fun trimOldMessages() {
        messageDao.deleteOld(retainCount = MESSAGE_RETENTION_LIMIT)
    }

    companion object {
        private const val MESSAGE_RETENTION_LIMIT = 50_000
    }
}
