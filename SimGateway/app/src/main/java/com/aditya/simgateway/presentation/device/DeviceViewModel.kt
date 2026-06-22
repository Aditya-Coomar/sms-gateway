package com.aditya.simgateway.presentation.device

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.aditya.simgateway.core.device.DeviceInfoProvider
import com.aditya.simgateway.core.device.NetworkInfoProvider
import com.aditya.simgateway.core.device.SimInfoProvider
import com.aditya.simgateway.domain.model.DeviceInfo
import com.aditya.simgateway.domain.model.NetworkInfo
import com.aditya.simgateway.domain.model.SimInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DeviceUiState(
    val device: DeviceInfo = DeviceInfo(
        deviceId = "Loading...",
        manufacturer = "Loading...",
        model = "Loading...",
        androidVersion = "Loading..."
    ),
    val simCards: List<SimInfo> = emptyList(),
    val network: NetworkInfo = NetworkInfo(
        connected = false,
        networkType = "NONE",
        metered = false
    )
)

class DeviceViewModel(application: Application) : AndroidViewModel(application) {

    private val deviceInfoProvider = DeviceInfoProvider(application)
    private val simInfoProvider = SimInfoProvider()
    private val networkInfoProvider = NetworkInfoProvider()

    private val _uiState = MutableStateFlow(DeviceUiState())
    val uiState: StateFlow<DeviceUiState> = _uiState.asStateFlow()

    init {
        loadDeviceInfo()
    }

    fun loadDeviceInfo() {
        val context = getApplication<Application>()
        viewModelScope.launch {
            val deviceInfo = deviceInfoProvider.getDeviceInfo()
            _uiState.value = DeviceUiState(
                device = deviceInfo,
                simCards = simInfoProvider.getSimInfo(context),
                network = networkInfoProvider.getNetworkInfo(context)
            )
        }
    }
}
