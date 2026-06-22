package com.aditya.simgateway.presentation.messages.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.aditya.simgateway.data.entity.MessageStatus
import com.aditya.simgateway.ui.theme.ErrorRed
import com.aditya.simgateway.ui.theme.GatewayGreen
import com.aditya.simgateway.ui.theme.WarningAmber

@Composable
fun MessageStatusBadge(status: MessageStatus) {
    val color = when (status) {
        MessageStatus.DELIVERED -> GatewayGreen
        MessageStatus.FAILED -> ErrorRed
        MessageStatus.QUEUED,
        MessageStatus.CREATED,
        MessageStatus.RETRYING,
        MessageStatus.SENDING -> WarningAmber
        MessageStatus.SENT -> MaterialTheme.colorScheme.primary
    }

    Text(
        text = status.name,
        color = color,
        style = MaterialTheme.typography.labelMedium,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    )
}
