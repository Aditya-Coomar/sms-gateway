package com.aditya.simgateway.data.repository

import com.aditya.simgateway.core.diagnostics.EventCategory
import com.aditya.simgateway.data.dao.EventLogDao
import com.aditya.simgateway.data.entity.EventLogEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class EventRepository(
    private val eventLogDao: EventLogDao
) {

    suspend fun writeEvent(
        type: EventCategory,
        source: String,
        message: String,
        payload: String? = null
    ) {
        eventLogDao.insertEvent(
            EventLogEntity(
                id = UUID.randomUUID().toString(),
                type = type,
                source = source,
                message = message,
                payload = payload,
                createdAt = System.currentTimeMillis()
            )
        )
        trimOldEvents()
    }

    fun readEvents(): Flow<List<EventLogEntity>> = eventLogDao.getEvents()

    fun filterEvents(type: EventCategory): Flow<List<EventLogEntity>> = eventLogDao.getByType(type)

    suspend fun trimOldEvents() {
        eventLogDao.deleteOldLogs(retainCount = EVENT_RETENTION_LIMIT)
    }

    companion object {
        private const val EVENT_RETENTION_LIMIT = 10_000
    }
}
