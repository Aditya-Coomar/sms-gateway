package com.aditya.simgateway.presentation.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.aditya.simgateway.core.diagnostics.EventCategory
import com.aditya.simgateway.core.diagnostics.EventLogger
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

    private var hasLoggedInitialSnapshot = false

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
        val nextState = DashboardUiState(
            health = GatewayHealthProvider.getHealth(),
            battery = batteryInfoProvider.getBatteryInfo(context),
            network = networkInfoProvider.getNetworkInfo(context),
            simCards = simInfoProvider.getSimInfo(context)
        )

        logStateTransitions(previous = _uiState.value, current = nextState)
        _uiState.value = nextState
    }

    private fun logStateTransitions(
        previous: DashboardUiState,
        current: DashboardUiState
    ) {
        if (!hasLoggedInitialSnapshot) {
            hasLoggedInitialSnapshot = true
            return
        }

        if (previous.network != current.network) {
            EventLogger.logEvent(
                category = EventCategory.NETWORK,
                source = "DashboardViewModel",
                message = "Network changed to ${current.network.networkType}",
                payload = "connected=${current.network.connected}, metered=${current.network.metered}"
            )
        }

        if (previous.simCards != current.simCards) {
            EventLogger.logEvent(
                category = EventCategory.DEVICE,
                source = "DashboardViewModel",
                message = "SIM configuration changed",
                payload = "simCount=${current.simCards.size}"
            )
        }

        if (previous.battery.level > 20 || previous.battery.level == -1) {
            if (current.battery.level in 0..20) {
                EventLogger.logWarning(
                    source = "DashboardViewModel",
                    message = "Battery low: ${current.battery.level}%"
                )
            }
        }
    }
}
