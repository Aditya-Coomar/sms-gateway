package com.aditya.simgateway.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.aditya.simgateway.SimGatewayApplication
import com.aditya.simgateway.core.sms.SmsConstants
import kotlinx.coroutines.launch

class SmsSentReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val messageId = intent.getStringExtra(SmsConstants.EXTRA_MESSAGE_ID) ?: return
        val pendingResult = goAsync()
        val application = context.applicationContext as SimGatewayApplication
        val broadcastResultCode = resultCode

        application.appContainer.applicationScope.launch {
            try {
                application.appContainer.smsGatewayManager.handleSentResult(
                    messageId = messageId,
                    resultCode = broadcastResultCode
                )
            } finally {
                pendingResult.finish()
            }
        }
    }
}
