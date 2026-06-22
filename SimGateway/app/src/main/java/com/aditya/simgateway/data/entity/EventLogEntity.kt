package com.aditya.simgateway.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.aditya.simgateway.core.diagnostics.EventCategory

@Entity(
    tableName = "event_logs",
    indices = [
        Index(value = ["createdAt"]),
        Index(value = ["type"])
    ]
)
data class EventLogEntity(
    @PrimaryKey
    val id: String,
    val type: EventCategory,
    val source: String,
    val message: String,
    val payload: String?,
    val createdAt: Long
)
