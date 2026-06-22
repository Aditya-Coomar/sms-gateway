package com.aditya.simgateway.core.sms

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.telephony.SmsManager
import android.telephony.SubscriptionInfo
import android.telephony.SubscriptionManager
import androidx.core.content.ContextCompat

class SimRouter(
    private val context: Context
) {

    @Suppress("DEPRECATION")
    fun getSmsManager(slotIndex: Int): SmsManager {
        val subscriptionId = resolveSubscriptionId(slotIndex)
        return SmsManager.getSmsManagerForSubscriptionId(subscriptionId)
    }

    fun resolveSubscriptionId(slotIndex: Int): Int {
        val subscription = getSubscriptionForSlot(slotIndex)
            ?: throw IllegalStateException("No active SIM found for slot ${slotIndex + 1}")
        return subscription.subscriptionId
    }

    fun hasActiveSim(slotIndex: Int): Boolean = getSubscriptionForSlot(slotIndex) != null

    fun getActiveSlots(): List<Int> = getActiveSubscriptions().map { it.simSlotIndex }

    private fun getSubscriptionForSlot(slotIndex: Int): SubscriptionInfo? {
        return getActiveSubscriptions().firstOrNull { it.simSlotIndex == slotIndex }
    }

    @Suppress("MissingPermission")
    private fun getActiveSubscriptions(): List<SubscriptionInfo> {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_PHONE_STATE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return emptyList()
        }

        val subscriptionManager = context.getSystemService(
            Context.TELEPHONY_SUBSCRIPTION_SERVICE
        ) as SubscriptionManager

        return subscriptionManager.activeSubscriptionInfoList.orEmpty()
    }
}
