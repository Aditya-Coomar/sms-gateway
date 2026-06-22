package com.aditya.simgateway.core.diagnostics

import com.aditya.simgateway.data.repository.EventRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

object EventLogger {

    private lateinit var repository: EventRepository
    private lateinit var scope: CoroutineScope

    fun initialize(
        repository: EventRepository,
        scope: CoroutineScope
    ) {
        this.repository = repository
        this.scope = scope
    }

    fun logInfo(
        source: String,
        message: String,
        payload: String? = null
    ) {
        logEvent(
            category = EventCategory.INFO,
            source = source,
            message = message,
            payload = payload
        )
    }

    fun logWarning(
        source: String,
        message: String,
        payload: String? = null
    ) {
        logEvent(
            category = EventCategory.WARNING,
            source = source,
            message = message,
            payload = payload
        )
    }

    fun logError(
        source: String,
        message: String,
        payload: String? = null
    ) {
        logEvent(
            category = EventCategory.ERROR,
            source = source,
            message = message,
            payload = payload
        )
    }

    fun logEvent(
        category: EventCategory,
        source: String,
        message: String,
        payload: String? = null
    ) {
        if (!::repository.isInitialized || !::scope.isInitialized) {
            return
        }

        scope.launch {
            repository.writeEvent(
                type = category,
                source = source,
                message = message,
                payload = payload
            )
        }
    }
}
