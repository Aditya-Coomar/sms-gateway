package com.aditya.simgateway.presentation.messages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Button
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
import com.aditya.simgateway.data.entity.MessageEntity
import com.aditya.simgateway.presentation.components.InfoRow
import com.aditya.simgateway.presentation.components.StatusCard
import com.aditya.simgateway.presentation.messages.components.MessageStatusBadge
import com.aditya.simgateway.ui.theme.DarkCard
import com.aditya.simgateway.ui.theme.GatewayGreen
import com.aditya.simgateway.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MessagesScreen(
    onOpenMessageDetails: (String) -> Unit,
    onOpenTestSms: () -> Unit,
    viewModel: MessagesViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val messages by viewModel.messages.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp, vertical = 24.dp)
    ) {
        Text(
            text = "Messages",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Message history and live lifecycle tracking",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onOpenTestSms) {
            Text("Send Test SMS")
        }

        Spacer(modifier = Modifier.height(16.dp))

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MessageFilter.entries.forEach { filter ->
                FilterChip(
                    selected = filter == state.filter,
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

        if (messages.isEmpty()) {
            StatusCard(
                title = "Messages",
                icon = Icons.Default.Sms
            ) {
                Text(
                    text = "No messages stored yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
            return
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(messages, key = { it.id }) { message ->
                MessageCard(
                    message = message,
                    onClick = { onOpenMessageDetails(message.id) }
                )
            }
        }
    }
}

@Composable
private fun MessageCard(
    message: MessageEntity,
    onClick: () -> Unit
) {
    StatusCard(
        title = message.recipient,
        icon = Icons.AutoMirrored.Filled.Send,
        iconTint = GatewayGreen,
        onClick = onClick
    ) {
        InfoRow(label = "Created", value = message.createdAt.asReadableTime())
        InfoRow(label = "SIM Slot", value = (message.simSlot + 1).toString())
        Spacer(modifier = Modifier.height(8.dp))
        MessageStatusBadge(status = message.status)
    }
}

private fun Long.asReadableTime(): String {
    val formatter = SimpleDateFormat("dd MMM yyyy, HH:mm:ss", Locale.getDefault())
    return formatter.format(Date(this))
}
