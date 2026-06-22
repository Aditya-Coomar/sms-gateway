package com.aditya.simgateway.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "device_config")
data class DeviceConfigEntity(
    @PrimaryKey
    val id: Int = 1,
    val serverUrl: String?,
    val deviceToken: String?,
    val paired: Boolean,
    val createdAt: Long,
    val updatedAt: Long
)
