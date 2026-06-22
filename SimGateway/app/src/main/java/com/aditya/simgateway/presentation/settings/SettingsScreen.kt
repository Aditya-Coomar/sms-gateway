package com.aditya.simgateway.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val saveMessage = state.saveMessage

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 24.dp)
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Persisted gateway configuration",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(24.dp))

        StatusCard(
            title = "Device Configuration",
            icon = Icons.Default.Settings,
            iconTint = GatewayGreen
        ) {
            OutlinedTextField(
                value = state.serverUrl,
                onValueChange = viewModel::updateServerUrl,
                label = { Text("Server URL") },
                singleLine = true,
                modifier = Modifier.fillMaxSize()
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = state.deviceToken,
                onValueChange = viewModel::updateDeviceToken,
                label = { Text("Device Token") },
                singleLine = true,
                modifier = Modifier.fillMaxSize()
            )
            Spacer(modifier = Modifier.height(12.dp))
            InfoRow(
                label = "Paired",
                value = if (state.paired) "Yes" else "No",
                valueColor = if (state.paired) GatewayGreen else TextSecondary
            )
            Switch(
                checked = state.paired,
                onCheckedChange = viewModel::updatePaired
            )
            Spacer(modifier = Modifier.height(12.dp))
            InfoRow(
                label = "Created",
                value = state.createdAt?.asReadableTime() ?: "Not saved"
            )
            InfoRow(
                label = "Updated",
                value = state.updatedAt?.asReadableTime() ?: "Not saved"
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = viewModel::saveConfig,
                enabled = !state.isSaving
            ) {
                Text(if (state.isSaving) "Saving..." else "Save Configuration")
            }
            if (!saveMessage.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = saveMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = GatewayGreen
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        StatusCard(
            title = "Persistence",
            icon = Icons.Default.Settings
        ) {
            Text(
                text = "Configuration, logs, and future message records are stored in Room for durable local operation.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

private fun Long.asReadableTime(): String {
    val formatter = SimpleDateFormat("dd MMM yyyy, HH:mm:ss", Locale.getDefault())
    return formatter.format(Date(this))
}
