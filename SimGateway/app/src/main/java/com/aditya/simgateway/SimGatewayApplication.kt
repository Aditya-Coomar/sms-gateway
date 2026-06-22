package com.aditya.simgateway

import android.app.Application
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.aditya.simgateway.core.diagnostics.EventCategory
import com.aditya.simgateway.core.diagnostics.EventLogger
import com.aditya.simgateway.data.database.GatewayDatabase
import com.aditya.simgateway.data.repository.DeviceRepository
import com.aditya.simgateway.data.repository.EventRepository
import com.aditya.simgateway.data.repository.MessageRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class SimGatewayApplication : Application(), DefaultLifecycleObserver {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    lateinit var appContainer: AppContainer
        private set

    override fun onCreate() {
        super<Application>.onCreate()

        val database = GatewayDatabase.create(this)
        appContainer = AppContainer(
            deviceRepository = DeviceRepository(database.deviceConfigDao()),
            messageRepository = MessageRepository(database.messageDao()),
            eventRepository = EventRepository(database.eventLogDao())
        )

        EventLogger.initialize(
            repository = appContainer.eventRepository,
            scope = applicationScope
        )
        EventLogger.logEvent(
            category = EventCategory.SYSTEM,
            source = "SimGatewayApplication",
            message = "App started"
        )

        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    override fun onStop(owner: LifecycleOwner) {
        EventLogger.logEvent(
            category = EventCategory.SYSTEM,
            source = "SimGatewayApplication",
            message = "App stopped"
        )
    }
}

data class AppContainer(
    val deviceRepository: DeviceRepository,
    val messageRepository: MessageRepository,
    val eventRepository: EventRepository
)
