package com.aditya.simgateway.presentation.logs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Article
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import com.aditya.simgateway.ui.theme.DarkCard
import com.aditya.simgateway.ui.theme.GatewayGreen
import com.aditya.simgateway.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun LogsScreen(
    viewModel: LogsViewModel = viewModel()
) {
    val logs by viewModel.events.collectAsState()
    val activeFilter by viewModel.activeFilter.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp, vertical = 24.dp)
    ) {
        Text(
            text = "Logs",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Gateway activity and diagnostics",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(16.dp))

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            LogFilter.entries.forEach { filter ->
                FilterChip(
                    selected = filter == activeFilter,
                    onClick = { viewModel.setFilter(filter) },
                    label = { Text(filter.label) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = GatewayGreen.copy(alpha = 0.15f),
                        containerColor = DarkCard
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (logs.isEmpty()) {
            StatusCard(
                title = "Event Viewer",
                icon = Icons.Default.Article
            ) {
                Text(
                    text = "No events available yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
            return
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(logs, key = { it.id }) { log ->
                StatusCard(
                    title = log.type.name,
                    icon = Icons.Default.Article,
                    iconTint = GatewayGreen
                ) {
                    InfoRow(label = "Timestamp", value = log.createdAt.asReadableTime())
                    InfoRow(label = "Source", value = log.source)
                    Text(
                        text = log.message,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (!log.payload.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = log.payload,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }
        }
    }
}

private fun Long.asReadableTime(): String {
    val formatter = SimpleDateFormat("dd MMM yyyy, HH:mm:ss", Locale.getDefault())
    return formatter.format(Date(this))
}
