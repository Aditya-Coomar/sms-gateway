package com.aditya.simgateway.core.device

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.telephony.SubscriptionManager
import androidx.core.content.ContextCompat
import com.aditya.simgateway.domain.model.SimInfo

class SimInfoProvider {

    @Suppress("MissingPermission")
    fun getSimInfo(context: Context): List<SimInfo> {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_PHONE_STATE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return emptyList()
        }

        val subscriptionManager =
            context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager

        val subscriptions = subscriptionManager.activeSubscriptionInfoList ?: return emptyList()

        return subscriptions.map { info ->
            SimInfo(
                slotIndex = info.simSlotIndex,
                carrierName = info.carrierName?.toString() ?: "Unknown",
                countryIso = info.countryIso ?: "Unknown",
                subscriptionId = info.subscriptionId
            )
        }
    }
}
