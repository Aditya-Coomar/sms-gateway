package com.aditya.simgateway

import android.app.Application
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.aditya.simgateway.core.diagnostics.EventCategory
import com.aditya.simgateway.core.diagnostics.EventLogger
import com.aditya.simgateway.core.sms.SimRouter
import com.aditya.simgateway.core.sms.SmsDispatcher
import com.aditya.simgateway.core.sms.SmsGatewayManager
import com.aditya.simgateway.core.sms.SmsRetryManager
import com.aditya.simgateway.core.sms.SmsValidator
import com.aditya.simgateway.data.database.GatewayDatabase
import com.aditya.simgateway.data.repository.DeviceRepository
import com.aditya.simgateway.data.repository.EventRepository
import com.aditya.simgateway.data.repository.MessageRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class SimGatewayApplication : Application(), DefaultLifecycleObserver {

    private val internalApplicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    lateinit var appContainer: AppContainer
        private set

    override fun onCreate() {
        super<Application>.onCreate()

        val database = GatewayDatabase.create(this)
        val deviceRepository = DeviceRepository(database.deviceConfigDao())
        val messageRepository = MessageRepository(database.messageDao())
        val eventRepository = EventRepository(database.eventLogDao())
        val smsGatewayManager = SmsGatewayManager(
            messageRepository = messageRepository,
            deviceRepository = deviceRepository,
            validator = SmsValidator(),
            simRouter = SimRouter(this),
            dispatcher = SmsDispatcher(this),
            retryManager = SmsRetryManager(internalApplicationScope)
        )
        appContainer = AppContainer(
            applicationScope = internalApplicationScope,
            deviceRepository = deviceRepository,
            messageRepository = messageRepository,
            eventRepository = eventRepository,
            smsGatewayManager = smsGatewayManager
        )

        EventLogger.initialize(
            repository = appContainer.eventRepository,
            scope = internalApplicationScope
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
    val applicationScope: CoroutineScope,
    val deviceRepository: DeviceRepository,
    val messageRepository: MessageRepository,
    val eventRepository: EventRepository,
    val smsGatewayManager: SmsGatewayManager
)
