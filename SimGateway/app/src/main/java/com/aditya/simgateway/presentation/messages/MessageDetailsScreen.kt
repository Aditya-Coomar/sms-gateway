package com.aditya.simgateway.presentation.messages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.aditya.simgateway.presentation.messages.components.MessageStatusBadge
import com.aditya.simgateway.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MessageDetailsScreen(
    messageId: String,
    onBack: () -> Unit,
    viewModel: MessagesViewModel = viewModel()
) {
    val message by viewModel.messageDetails(messageId).collectAsState(initial = null)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 24.dp)
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back"
            )
        }

        Text(
            text = "Message Details",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (message == null) {
            StatusCard(
                title = "Details",
                icon = Icons.Default.Info
            ) {
                Text(
                    text = "Message not found.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
            return
        }

        val currentMessage = message ?: return
        StatusCard(
            title = currentMessage.recipient,
            icon = Icons.Default.Info
        ) {
            MessageStatusBadge(currentMessage.status)
            Spacer(modifier = Modifier.height(12.dp))
            InfoRow(label = "Recipient", value = currentMessage.recipient)
            InfoRow(label = "SIM Slot", value = (currentMessage.simSlot + 1).toString())
            InfoRow(label = "Created", value = currentMessage.createdAt.asReadableTime())
            InfoRow(label = "Sent", value = currentMessage.sentAt?.asReadableTime() ?: "Pending")
            InfoRow(
                label = "Delivered",
                value = currentMessage.deliveredAt?.asReadableTime() ?: "Pending"
            )
            InfoRow(label = "Retries", value = currentMessage.retryCount.toString())
            if (!currentMessage.failureReason.isNullOrBlank()) {
                InfoRow(label = "Failure", value = currentMessage.failureReason ?: "")
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = currentMessage.body,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

private fun Long.asReadableTime(): String {
    val formatter = SimpleDateFormat("dd MMM yyyy, HH:mm:ss", Locale.getDefault())
    return formatter.format(Date(this))
}
