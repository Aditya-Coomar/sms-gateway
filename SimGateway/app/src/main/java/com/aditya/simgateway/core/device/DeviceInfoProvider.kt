package com.aditya.simgateway.core.device

import android.content.Context
import android.os.Build
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.aditya.simgateway.domain.model.DeviceInfo
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.UUID

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "device_prefs")

class DeviceInfoProvider(private val context: Context) {

    companion object {
        private val DEVICE_ID_KEY = stringPreferencesKey("device_id")
    }

    suspend fun getDeviceInfo(): DeviceInfo {
        val deviceId = getOrCreateDeviceId()
        return DeviceInfo(
            deviceId = deviceId,
            manufacturer = Build.MANUFACTURER,
            model = Build.MODEL,
            androidVersion = Build.VERSION.RELEASE
        )
    }

    private suspend fun getOrCreateDeviceId(): String {
        val existing = context.dataStore.data
            .map { prefs -> prefs[DEVICE_ID_KEY] }
            .first()

        if (existing != null) return existing

        val newId = UUID.randomUUID().toString()
        context.dataStore.edit { prefs ->
            prefs[DEVICE_ID_KEY] = newId
        }
        return newId
    }
}
