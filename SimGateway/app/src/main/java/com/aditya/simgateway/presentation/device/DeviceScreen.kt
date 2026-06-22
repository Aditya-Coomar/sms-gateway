package com.aditya.simgateway.presentation.device

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aditya.simgateway.presentation.components.InfoRow
import com.aditya.simgateway.presentation.components.StatusCard
import com.aditya.simgateway.ui.theme.GatewayGreen
import com.aditya.simgateway.ui.theme.TextSecondary

@Composable
fun DeviceScreen(
    viewModel: DeviceViewModel = viewModel()
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
            text = "Device",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Hardware Diagnostics",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Device Section
        StatusCard(
            title = "Device Info",
            icon = Icons.Default.Phone
        ) {
            InfoRow(label = "Manufacturer", value = state.device.manufacturer)
            InfoRow(label = "Model", value = state.device.model)
            InfoRow(label = "Android Version", value = state.device.androidVersion)
            InfoRow(label = "Device ID", value = state.device.deviceId)
        }

        Spacer(modifier = Modifier.height(12.dp))

        // SIM Section
        if (state.simCards.isNotEmpty()) {
            state.simCards.forEachIndexed { index, sim ->
                StatusCard(
                    title = "SIM ${index + 1}",
                    icon = Icons.Default.Phone,
                    iconTint = GatewayGreen
                ) {
                    InfoRow(label = "Carrier", value = sim.carrierName)
                    InfoRow(label = "Country", value = sim.countryIso.uppercase())
                    InfoRow(label = "Subscription ID", value = sim.subscriptionId.toString())
                    InfoRow(label = "Slot", value = "${sim.slotIndex + 1}")
                }
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
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Network Section
        StatusCard(
            title = "Network",
            icon = Icons.Default.Info,
            iconTint = if (state.network.connected) GatewayGreen else TextSecondary
        ) {
            InfoRow(
                label = "Connected",
                value = if (state.network.connected) "Yes" else "No",
                valueColor = if (state.network.connected) GatewayGreen else TextSecondary
            )
            InfoRow(label = "Type", value = state.network.networkType)
            InfoRow(
                label = "Metered",
                value = if (state.network.metered) "Yes" else "No"
            )
        }

        // Bottom spacing for navigation bar
        Spacer(modifier = Modifier.height(16.dp))
    }
}
