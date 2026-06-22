package com.aditya.simgateway.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.aditya.simgateway.data.entity.DeviceConfigEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DeviceConfigDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConfig(config: DeviceConfigEntity)

    @Update
    suspend fun updateConfig(config: DeviceConfigEntity)

    @Query("SELECT * FROM device_config WHERE id = 1 LIMIT 1")
    suspend fun getConfig(): DeviceConfigEntity?

    @Query("SELECT * FROM device_config WHERE id = 1 LIMIT 1")
    fun observeConfig(): Flow<DeviceConfigEntity?>

    @Query("DELETE FROM device_config WHERE id = 1")
    suspend fun deleteConfig()
}
