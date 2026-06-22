package com.aditya.simgateway.presentation.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.aditya.simgateway.core.device.BatteryInfoProvider
import com.aditya.simgateway.core.device.GatewayHealthProvider
import com.aditya.simgateway.core.device.NetworkInfoProvider
import com.aditya.simgateway.core.device.SimInfoProvider
import com.aditya.simgateway.domain.model.BatteryInfo
import com.aditya.simgateway.domain.model.GatewayHealth
import com.aditya.simgateway.domain.model.NetworkInfo
import com.aditya.simgateway.domain.model.SimInfo
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class DashboardUiState(
    val health: GatewayHealth = GatewayHealth(
        serviceRunning = false,
        uptimeSeconds = 0L,
        lastStartedAt = 0L
    ),
    val battery: BatteryInfo = BatteryInfo(
        level = -1,
        charging = false,
        powerSource = "Unknown"
    ),
    val network: NetworkInfo = NetworkInfo(
        connected = false,
        networkType = "NONE",
        metered = false
    ),
    val simCards: List<SimInfo> = emptyList()
)

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val batteryInfoProvider = BatteryInfoProvider()
    private val networkInfoProvider = NetworkInfoProvider()
    private val simInfoProvider = SimInfoProvider()

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        startPeriodicRefresh()
    }

    private fun startPeriodicRefresh() {
        viewModelScope.launch {
            while (isActive) {
                refreshData()
                delay(5_000L)
            }
        }
    }

    fun refreshData() {
        val context = getApplication<Application>()
        _uiState.value = DashboardUiState(
            health = GatewayHealthProvider.getHealth(),
            battery = batteryInfoProvider.getBatteryInfo(context),
            network = networkInfoProvider.getNetworkInfo(context),
            simCards = simInfoProvider.getSimInfo(context)
        )
    }
}
