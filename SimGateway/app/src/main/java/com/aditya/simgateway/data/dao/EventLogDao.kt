package com.aditya.simgateway.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.aditya.simgateway.core.diagnostics.EventCategory
import com.aditya.simgateway.data.entity.EventLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EventLogDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: EventLogEntity)

    @Query("SELECT * FROM event_logs ORDER BY createdAt DESC")
    fun getEvents(): Flow<List<EventLogEntity>>

    @Query("SELECT * FROM event_logs WHERE type = :type ORDER BY createdAt DESC")
    fun getByType(type: EventCategory): Flow<List<EventLogEntity>>

    @Query(
        """
        DELETE FROM event_logs
        WHERE id NOT IN (
            SELECT id FROM event_logs
            ORDER BY createdAt DESC
            LIMIT :retainCount
        )
        """
    )
    suspend fun deleteOldLogs(retainCount: Int)
}
