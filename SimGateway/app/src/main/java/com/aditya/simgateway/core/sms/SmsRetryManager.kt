package com.aditya.simgateway.core.sms

import com.aditya.simgateway.core.diagnostics.EventLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SmsRetryManager(
    private val scope: CoroutineScope
) {

    fun scheduleRetry(
        messageId: String,
        retryCount: Int,
        maxRetryCount: Int,
        onRetry: suspend (String) -> Unit
    ): Boolean {
        if (retryCount >= maxRetryCount || retryCount >= RETRY_DELAYS_MS.size) {
            return false
        }

        val delayMs = RETRY_DELAYS_MS[retryCount]
        EventLogger.logWarning(
            source = "SmsRetryManager",
            message = "Retry scheduled",
            payload = "messageId=$messageId, retryCount=${retryCount + 1}, delayMs=$delayMs"
        )

        scope.launch {
            delay(delayMs)
            onRetry(messageId)
        }
        return true
    }

    private companion object {
        val RETRY_DELAYS_MS = listOf(0L, 30_000L, 60_000L, 300_000L)
    }
}
