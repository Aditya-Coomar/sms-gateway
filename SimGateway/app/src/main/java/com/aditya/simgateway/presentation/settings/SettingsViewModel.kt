package com.aditya.simgateway.presentation.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.aditya.simgateway.SimGatewayApplication
import com.aditya.simgateway.core.diagnostics.EventCategory
import com.aditya.simgateway.core.diagnostics.EventLogger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsUiState(
    val serverUrl: String = "",
    val deviceToken: String = "",
    val paired: Boolean = false,
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

    fun saveConfig() {
        viewModelScope.launch {
            val snapshot = _uiState.value
            _uiState.update { it.copy(isSaving = true, saveMessage = null) }

            deviceRepository.saveConfig(
                serverUrl = snapshot.serverUrl.ifBlank { null },
                deviceToken = snapshot.deviceToken.ifBlank { null },
                paired = snapshot.paired
            )

            EventLogger.logEvent(
                category = EventCategory.SYSTEM,
                source = "SettingsViewModel",
                message = "Device configuration saved"
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
