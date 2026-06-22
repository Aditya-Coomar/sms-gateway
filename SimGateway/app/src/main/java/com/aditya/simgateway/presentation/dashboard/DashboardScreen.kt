package com.aditya.simgateway.presentation.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aditya.simgateway.presentation.components.InfoRow
import com.aditya.simgateway.presentation.components.StatusCard
import com.aditya.simgateway.ui.theme.ErrorRed
import com.aditya.simgateway.ui.theme.GatewayGreen
import com.aditya.simgateway.ui.theme.TextSecondary
import com.aditya.simgateway.ui.theme.WarningAmber

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 24.dp)
    ) {
        // Header
        Text(
            text = "SMS Gateway",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Gateway Overview",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Gateway Status Card
        GatewayStatusCard(
            running = state.health.serviceRunning,
            uptimeSeconds = state.health.uptimeSeconds
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Battery Card
        BatteryCard(
            level = state.battery.level,
            charging = state.battery.charging,
            powerSource = state.battery.powerSource
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Network Card
        NetworkCard(
            connected = state.network.connected,
            networkType = state.network.networkType
        )

        Spacer(modifier = Modifier.height(12.dp))

        // SIM Cards
        if (state.simCards.isNotEmpty()) {
            state.simCards.forEach { sim ->
                SimCard(
                    carrierName = sim.carrierName,
                    slotIndex = sim.slotIndex
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        } else {
            StatusCard(
                title = "SIM",
                icon = Icons.Default.Phone,
                iconTint = TextSecondary
            ) {
                Text(
                    text = "No SIM detected or permission required",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        }

        // Bottom spacing for navigation bar
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun GatewayStatusCard(
    running: Boolean,
    uptimeSeconds: Long
) {
    StatusCard(
        title = "Gateway Status",
        icon = Icons.Default.Notifications,
        iconTint = if (running) GatewayGreen else ErrorRed
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(if (running) GatewayGreen else ErrorRed)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (running) "Running" else "Stopped",
                style = MaterialTheme.typography.titleMedium,
                color = if (running) GatewayGreen else ErrorRed
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        InfoRow(label = "Uptime", value = formatUptime(uptimeSeconds))
        InfoRow(
            label = "Foreground Service",
            value = if (running) "Active" else "Inactive",
            valueColor = if (running) GatewayGreen else ErrorRed
        )
    }
}

@Composable
private fun BatteryCard(
    level: Int,
    charging: Boolean,
    powerSource: String
) {
    StatusCard(
        title = "Battery",
        icon = Icons.Default.FavoriteBorder,
        iconTint = when {
            level <= 20 -> ErrorRed
            level <= 50 -> WarningAmber
            else -> GatewayGreen
        }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Battery: ${level}%",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = powerSource,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { (level.coerceIn(0, 100)) / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(CircleShape),
            color = when {
                level <= 20 -> ErrorRed
                level <= 50 -> WarningAmber
                else -> GatewayGreen
            },
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )
        Spacer(modifier = Modifier.height(8.dp))
        InfoRow(
            label = "Charging",
            value = if (charging) "Yes" else "No",
            valueColor = if (charging) GatewayGreen else TextSecondary
        )
    }
}

@Composable
private fun NetworkCard(
    connected: Boolean,
    networkType: String
) {
    StatusCard(
        title = "Network",
        icon = Icons.Default.Info,
        iconTint = if (connected) GatewayGreen else ErrorRed
    ) {
        Text(
            text = networkType,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = if (connected) "Connected" else "Disconnected",
            style = MaterialTheme.typography.bodyMedium,
            color = if (connected) GatewayGreen else ErrorRed
        )
    }
}

@Composable
private fun SimCard(
    carrierName: String,
    slotIndex: Int
) {
    StatusCard(
        title = "SIM",
        icon = Icons.Default.Phone,
        iconTint = GatewayGreen
    ) {
        Text(
            text = carrierName,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(4.dp))
        InfoRow(label = "Slot", value = "${slotIndex + 1}")
        InfoRow(label = "Status", value = "Ready", valueColor = GatewayGreen)
    }
}

private fun formatUptime(seconds: Long): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val secs = seconds % 60
    return when {
        hours > 0 -> "${hours}h ${minutes}m ${secs}s"
        minutes > 0 -> "${minutes}m ${secs}s"
        else -> "${secs}s"
    }
}
