package com.aditya.simgateway.core.device

import android.content.Context
import android.os.Build
import android.provider.Settings
import com.aditya.simgateway.domain.model.DeviceInfo

class DeviceInfoProvider(private val context: Context) {

    suspend fun getDeviceInfo(): DeviceInfo {
        val deviceId = getStableDeviceId()
        return DeviceInfo(
            deviceId = deviceId,
            manufacturer = Build.MANUFACTURER,
            model = Build.MODEL,
            androidVersion = Build.VERSION.RELEASE
        )
    }

    private fun getStableDeviceId(): String {
        val androidId = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID
        )?.lowercase().orEmpty()

        return if (androidId.isNotBlank() && androidId != UNKNOWN_ANDROID_ID) {
            "dev_$androidId"
        } else {
            "dev_${Build.BRAND.lowercase()}_${Build.MODEL.lowercase()}"
        }
    }

    private companion object {
        const val UNKNOWN_ANDROID_ID = "9774d56d682e549c"
    }
}
