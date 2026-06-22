package com.aditya.simgateway.data.repository

import com.aditya.simgateway.data.dao.DeviceConfigDao
import com.aditya.simgateway.data.entity.DeviceConfigEntity
import kotlinx.coroutines.flow.Flow

class DeviceRepository(
    private val deviceConfigDao: DeviceConfigDao
) {

    suspend fun saveConfig(
        serverUrl: String?,
        deviceToken: String?,
        paired: Boolean
    ) {
        val now = System.currentTimeMillis()
        val existingConfig = deviceConfigDao.getConfig()
        deviceConfigDao.insertConfig(
            DeviceConfigEntity(
                serverUrl = serverUrl,
                deviceToken = deviceToken,
                paired = paired,
                createdAt = existingConfig?.createdAt ?: now,
                updatedAt = now
            )
        )
    }

    suspend fun loadConfig(): DeviceConfigEntity? = deviceConfigDao.getConfig()

    fun observeConfig(): Flow<DeviceConfigEntity?> = deviceConfigDao.observeConfig()

    suspend fun updatePairingState(paired: Boolean) {
        val existingConfig = deviceConfigDao.getConfig()
        val now = System.currentTimeMillis()
        deviceConfigDao.insertConfig(
            DeviceConfigEntity(
                serverUrl = existingConfig?.serverUrl,
                deviceToken = existingConfig?.deviceToken,
                paired = paired,
                createdAt = existingConfig?.createdAt ?: now,
                updatedAt = now
            )
        )
    }

    suspend fun deleteConfig() {
        deviceConfigDao.deleteConfig()
    }
}
