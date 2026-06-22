package com.aditya.simgateway.core.sms

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.telephony.SmsManager
import com.aditya.simgateway.receivers.SmsDeliveredReceiver
import com.aditya.simgateway.receivers.SmsSentReceiver

class SmsDispatcher(
    private val context: Context
) {

    fun dispatch(
        messageId: String,
        recipient: String,
        body: String,
        simSlot: Int,
        smsManager: SmsManager,
        requestDeliveryReport: Boolean
    ) {
        val sentIntent = PendingIntent.getBroadcast(
            context,
            messageId.hashCode(),
            Intent(context, SmsSentReceiver::class.java)
                .putExtra(SmsConstants.EXTRA_MESSAGE_ID, messageId)
                .putExtra(SmsConstants.EXTRA_SIM_SLOT, simSlot),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val deliveredIntent = if (requestDeliveryReport) {
            PendingIntent.getBroadcast(
                context,
                messageId.hashCode() + 1,
                Intent(context, SmsDeliveredReceiver::class.java)
                    .putExtra(SmsConstants.EXTRA_MESSAGE_ID, messageId)
                    .putExtra(SmsConstants.EXTRA_SIM_SLOT, simSlot),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        } else {
            null
        }

        smsManager.sendTextMessage(
            recipient,
            null,
            body,
            sentIntent,
            deliveredIntent
        )
    }
}
