package com.aditya.simgateway.core.sms

import android.app.Activity
import com.aditya.simgateway.core.diagnostics.EventCategory
import com.aditya.simgateway.core.diagnostics.EventLogger
import com.aditya.simgateway.data.entity.MessageEntity
import com.aditya.simgateway.data.entity.MessageStatus
import com.aditya.simgateway.data.repository.DeviceRepository
import com.aditya.simgateway.data.repository.MessageRepository

class SmsGatewayManager(
    private val messageRepository: MessageRepository,
    private val deviceRepository: DeviceRepository,
    private val validator: SmsValidator,
    private val simRouter: SimRouter,
    private val dispatcher: SmsDispatcher,
    private val retryManager: SmsRetryManager
) {

    suspend fun sendSms(
        recipient: String,
        message: String,
        simSlot: Int
    ): SmsSendResult {
        return when (val validation = validator.validate(recipient, message)) {
            is SmsValidationResult.Invalid -> {
                EventLogger.logError(
                    source = "SmsGatewayManager",
                    message = "SMS validation failed",
                    payload = validation.reason
                )
                SmsSendResult.Failure(validation.reason)
            }

            is SmsValidationResult.Valid -> {
                if (!simRouter.hasActiveSim(simSlot)) {
                    val reason = "Selected SIM slot is not active"
                    EventLogger.logError(
                        source = "SmsGatewayManager",
                        message = "SMS send aborted",
                        payload = reason
                    )
                    return SmsSendResult.Failure(reason)
                }

                val createdMessage = messageRepository.createMessage(
                    recipient = validation.recipient,
                    body = validation.message,
                    simSlot = simSlot,
                    status = MessageStatus.CREATED
                )
                EventLogger.logEvent(
                    category = EventCategory.SMS,
                    source = "SmsGatewayManager",
                    message = "SMS created",
                    payload = "messageId=${createdMessage.id}"
                )

                val queuedMessage = createdMessage.copy(status = MessageStatus.QUEUED)
                messageRepository.upsertMessage(queuedMessage)
                EventLogger.logEvent(
                    category = EventCategory.SMS,
                    source = "SmsGatewayManager",
                    message = "SMS queued",
                    payload = "messageId=${queuedMessage.id}, simSlot=${queuedMessage.simSlot + 1}"
                )

                return dispatchMessage(queuedMessage)
            }
        }
    }

    suspend fun retryMessage(messageId: String) {
        val existingMessage = messageRepository.getMessageById(messageId) ?: return
        val retryingMessage = existingMessage.copy(
            status = MessageStatus.RETRYING,
            retryCount = existingMessage.retryCount + 1,
            failureReason = null
        )
        messageRepository.upsertMessage(retryingMessage)
        EventLogger.logEvent(
            category = EventCategory.SMS,
            source = "SmsGatewayManager",
            message = "Retrying SMS",
            payload = "messageId=${retryingMessage.id}, retryCount=${retryingMessage.retryCount}"
        )
        dispatchMessage(retryingMessage)
    }

    suspend fun handleSentResult(
        messageId: String,
        resultCode: Int
    ) {
        val existingMessage = messageRepository.getMessageById(messageId) ?: return
        if (resultCode == Activity.RESULT_OK) {
            val sentMessage = existingMessage.copy(
                status = MessageStatus.SENT,
                sentAt = System.currentTimeMillis(),
                failureReason = null
            )
            messageRepository.upsertMessage(sentMessage)
            EventLogger.logEvent(
                category = EventCategory.SMS,
                source = "SmsSentReceiver",
                message = "SMS sent",
                payload = "messageId=${sentMessage.id}"
            )
        } else {
            handleSendFailure(existingMessage, "SMS sending failed (code=$resultCode)")
        }
    }

    suspend fun handleDeliveredResult(
        messageId: String,
        resultCode: Int
    ) {
        val existingMessage = messageRepository.getMessageById(messageId) ?: return
        if (resultCode == Activity.RESULT_OK) {
            val deliveredMessage = existingMessage.copy(
                status = MessageStatus.DELIVERED,
                deliveredAt = System.currentTimeMillis(),
                failureReason = null
            )
            messageRepository.upsertMessage(deliveredMessage)
            EventLogger.logEvent(
                category = EventCategory.SMS,
                source = "SmsDeliveredReceiver",
                message = "SMS delivered",
                payload = "messageId=${deliveredMessage.id}"
            )
        } else {
            EventLogger.logWarning(
                source = "SmsDeliveredReceiver",
                message = "Delivery report received without confirmation",
                payload = "messageId=$messageId, resultCode=$resultCode"
            )
        }
    }

    suspend fun getDefaultSimSlot(): Int? = deviceRepository.loadConfig()?.defaultSimSlot

    private suspend fun dispatchMessage(message: MessageEntity): SmsSendResult {
        return runCatching {
            val sendingMessage = message.copy(
                status = MessageStatus.SENDING,
                failureReason = null
            )
            messageRepository.upsertMessage(sendingMessage)

            val config = deviceRepository.loadConfig()
            val smsManager = simRouter.getSmsManager(sendingMessage.simSlot)
            dispatcher.dispatch(
                messageId = sendingMessage.id,
                recipient = sendingMessage.recipient,
                body = sendingMessage.body,
                simSlot = sendingMessage.simSlot,
                smsManager = smsManager,
                requestDeliveryReport = config?.deliveryReportsEnabled ?: true
            )
            EventLogger.logEvent(
                category = EventCategory.SMS,
                source = "SmsGatewayManager",
                message = "SMS sending started",
                payload = "messageId=${sendingMessage.id}"
            )
            SmsSendResult.Success(sendingMessage.id)
        }.getOrElse { throwable ->
            handleSendFailure(message, throwable.message ?: "Unknown SMS send failure")
            SmsSendResult.Failure(throwable.message ?: "Unknown SMS send failure")
        }
    }

    private suspend fun handleSendFailure(
        message: MessageEntity,
        reason: String
    ) {
        val config = deviceRepository.loadConfig()
        val failedMessage = message.copy(
            status = MessageStatus.FAILED,
            failureReason = reason
        )
        messageRepository.upsertMessage(failedMessage)
        EventLogger.logEvent(
            category = EventCategory.SMS,
            source = "SmsGatewayManager",
            message = "SMS failed",
            payload = "messageId=${message.id}, reason=$reason"
        )

        val maxRetryCount = config?.maxRetryCount ?: DeviceRepository.DEFAULT_MAX_RETRY_COUNT
        retryManager.scheduleRetry(
            messageId = message.id,
            retryCount = message.retryCount,
            maxRetryCount = maxRetryCount,
            onRetry = ::retryMessage
        )
    }
}

sealed interface SmsSendResult {
    data class Success(val messageId: String) : SmsSendResult
    data class Failure(val reason: String) : SmsSendResult
}
