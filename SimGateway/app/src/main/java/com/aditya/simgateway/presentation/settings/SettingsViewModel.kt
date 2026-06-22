package com.aditya.simgateway.presentation.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.aditya.simgateway.SimGatewayApplication
import com.aditya.simgateway.core.diagnostics.EventCategory
import com.aditya.simgateway.core.diagnostics.EventLogger
import com.aditya.simgateway.data.repository.DeviceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsUiState(
    val serverUrl: String = "",
    val deviceToken: String = "",
    val paired: Boolean = false,
    val defaultSimSlot: String = "",
    val retryCount: String = DeviceRepository.DEFAULT_MAX_RETRY_COUNT.toString(),
    val deliveryReportsEnabled: Boolean = true,
    val createdAt: Long? = null,
    val updatedAt: Long? = null,
    val isSaving: Boolean = false,
    val saveMessage: String? = null
)

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val deviceRepository = (application as SimGatewayApplication)
        .appContainer
        .deviceRepository

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            deviceRepository.observeConfig().collect { config ->
                _uiState.update { current ->
                    current.copy(
                        serverUrl = config?.serverUrl.orEmpty(),
                        deviceToken = config?.deviceToken.orEmpty(),
                        paired = config?.paired ?: false,
                        defaultSimSlot = config?.defaultSimSlot?.plus(1)?.toString().orEmpty(),
                        retryCount = (config?.maxRetryCount ?: DeviceRepository.DEFAULT_MAX_RETRY_COUNT)
                            .toString(),
                        deliveryReportsEnabled = config?.deliveryReportsEnabled ?: true,
                        createdAt = config?.createdAt,
                        updatedAt = config?.updatedAt,
                        isSaving = false
                    )
                }
            }
        }
    }

    fun updateServerUrl(value: String) {
        _uiState.update { it.copy(serverUrl = value, saveMessage = null) }
    }

    fun updateDeviceToken(value: String) {
        _uiState.update { it.copy(deviceToken = value, saveMessage = null) }
    }

    fun updatePaired(value: Boolean) {
        _uiState.update { it.copy(paired = value, saveMessage = null) }
    }

    fun updateDefaultSimSlot(value: String) {
        _uiState.update { it.copy(defaultSimSlot = value.filter(Char::isDigit), saveMessage = null) }
    }

    fun updateRetryCount(value: String) {
        _uiState.update { it.copy(retryCount = value.filter(Char::isDigit), saveMessage = null) }
    }

    fun updateDeliveryReportsEnabled(value: Boolean) {
        _uiState.update { it.copy(deliveryReportsEnabled = value, saveMessage = null) }
    }

    fun saveConfig() {
        viewModelScope.launch {
            val snapshot = _uiState.value
            _uiState.update { it.copy(isSaving = true, saveMessage = null) }

            val retryCount = snapshot.retryCount.toIntOrNull()
                ?.coerceIn(1, DeviceRepository.DEFAULT_MAX_RETRY_COUNT)
                ?: DeviceRepository.DEFAULT_MAX_RETRY_COUNT
            val defaultSimSlot = snapshot.defaultSimSlot.toIntOrNull()
                ?.takeIf { it > 0 }
                ?.minus(1)

            deviceRepository.saveConfig(
                serverUrl = snapshot.serverUrl.ifBlank { null },
                deviceToken = snapshot.deviceToken.ifBlank { null },
                paired = snapshot.paired,
                defaultSimSlot = defaultSimSlot,
                maxRetryCount = retryCount,
                deliveryReportsEnabled = snapshot.deliveryReportsEnabled
            )

            EventLogger.logEvent(
                category = EventCategory.SYSTEM,
                source = "SettingsViewModel",
                message = "Device configuration saved",
                payload = "defaultSimSlot=$defaultSimSlot, retryCount=$retryCount"
            )

            _uiState.update {
                it.copy(
                    isSaving = false,
                    saveMessage = "Configuration saved locally"
                )
            }
        }
    }
}
