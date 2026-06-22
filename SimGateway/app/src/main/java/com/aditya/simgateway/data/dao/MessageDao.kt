package com.aditya.simgateway.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.aditya.simgateway.data.entity.MessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)

    @Query("SELECT * FROM messages WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): MessageEntity?

    @Query("SELECT * FROM messages WHERE id = :id LIMIT 1")
    fun observeById(id: String): Flow<MessageEntity?>

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
