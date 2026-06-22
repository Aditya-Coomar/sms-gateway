package com.aditya.simgateway.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "messages",
    indices = [
        Index(value = ["createdAt"]),
        Index(value = ["status"])
    ]
)
data class MessageEntity(
    @PrimaryKey
    val id: String,
    val recipient: String,
    val body: String,
    val simSlot: Int,
    val status: MessageStatus,
    val createdAt: Long,
    val sentAt: Long?,
    val deliveredAt: Long?,
    val errorMessage: String?
)

enum class MessageStatus {
    QUEUED,
    PENDING,
    SENDING,
    SENT,
    DELIVERED,
    FAILED
}
