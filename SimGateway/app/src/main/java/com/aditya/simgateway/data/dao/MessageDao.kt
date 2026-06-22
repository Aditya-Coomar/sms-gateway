package com.aditya.simgateway.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.aditya.simgateway.data.entity.MessageEntity
import com.aditya.simgateway.data.entity.MessageStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)

    @Query(
        """
        UPDATE messages
        SET status = :status,
            sentAt = :sentAt,
            deliveredAt = :deliveredAt,
            errorMessage = :errorMessage
        WHERE id = :id
        """
    )
    suspend fun updateStatus(
        id: String,
        status: MessageStatus,
        sentAt: Long?,
        deliveredAt: Long?,
        errorMessage: String?
    )

    @Query("SELECT * FROM messages WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): MessageEntity?

    @Query("SELECT * FROM messages ORDER BY createdAt DESC")
    fun getAll(): Flow<List<MessageEntity>>

    @Query(
        """
        DELETE FROM messages
        WHERE id NOT IN (
            SELECT id FROM messages
            ORDER BY createdAt DESC
            LIMIT :retainCount
        )
        """
    )
    suspend fun deleteOld(retainCount: Int)
}
