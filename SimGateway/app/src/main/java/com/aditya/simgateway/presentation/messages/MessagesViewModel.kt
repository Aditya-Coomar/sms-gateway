package com.aditya.simgateway.presentation.messages

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.aditya.simgateway.SimGatewayApplication
import com.aditya.simgateway.core.device.SimInfoProvider
import com.aditya.simgateway.core.sms.SmsGatewayManager
import com.aditya.simgateway.core.sms.SmsSendResult
import com.aditya.simgateway.data.entity.MessageEntity
import com.aditya.simgateway.data.entity.MessageStatus
import com.aditya.simgateway.data.repository.MessageRepository
import com.aditya.simgateway.domain.model.SimInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class MessageFilter(val label: String) {
    ALL("All"),
    QUEUED("Queued"),
    SENT("Sent"),
    DELIVERED("Delivered"),
    FAILED("Failed")
}

data class MessagesUiState(
    val filter: MessageFilter = MessageFilter.ALL,
    val recipient: String = "",
    val body: String = "",
    val selectedSimSlot: Int = 0,
    val sendResultMessage: String? = null,
    val isSending: Boolean = false,
    val defaultSimSlot: Int? = null,
    val simOptions: List<SimInfo> = emptyList()
)

class MessagesViewModel(application: Application) : AndroidViewModel(application) {

    private val appContainer = (application as SimGatewayApplication).appContainer
    private val messageRepository: MessageRepository = appContainer.messageRepository
    private val smsGatewayManager: SmsGatewayManager = appContainer.smsGatewayManager
    private val simInfoProvider = SimInfoProvider()
    private val formState = MutableStateFlow(MessagesUiState())

    val uiState: StateFlow<MessagesUiState> = formState

    val messages: StateFlow<List<MessageEntity>> = combine(
        messageRepository.fetchMessageHistory(),
        formState
    ) { messages, state ->
        messages.filter { message ->
            when (state.filter) {
                MessageFilter.ALL -> true
                MessageFilter.QUEUED -> message.status in setOf(
                    MessageStatus.CREATED,
                    MessageStatus.QUEUED,
                    MessageStatus.SENDING,
                    MessageStatus.RETRYING
                )
                MessageFilter.SENT -> message.status == MessageStatus.SENT
                MessageFilter.DELIVERED -> message.status == MessageStatus.DELIVERED
                MessageFilter.FAILED -> message.status == MessageStatus.FAILED
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = emptyList()
    )

    init {
        refreshSimOptions()
        viewModelScope.launch {
            val defaultSimSlot = smsGatewayManager.getDefaultSimSlot()
            formState.update {
                it.copy(
                    selectedSimSlot = defaultSimSlot ?: it.selectedSimSlot,
                    defaultSimSlot = defaultSimSlot
                )
            }
        }
    }

    fun messageDetails(messageId: String): Flow<MessageEntity?> {
        return messageRepository.observeMessageById(messageId)
    }

    fun setFilter(filter: MessageFilter) {
        formState.update { it.copy(filter = filter) }
    }

    fun updateRecipient(value: String) {
        formState.update { it.copy(recipient = value, sendResultMessage = null) }
    }

    fun updateBody(value: String) {
        formState.update { it.copy(body = value, sendResultMessage = null) }
    }

    fun updateSelectedSimSlot(value: String) {
        val slot = value.toIntOrNull()?.minus(1) ?: 0
        formState.update {
            it.copy(
                selectedSimSlot = slot.coerceAtLeast(0),
                sendResultMessage = null
            )
        }
    }

    fun refreshSimOptions() {
        val sims = simInfoProvider.getSimInfo(getApplication())
        formState.update { state ->
            state.copy(
                simOptions = sims,
                selectedSimSlot = state.defaultSimSlot ?: sims.firstOrNull()?.slotIndex ?: state.selectedSimSlot
            )
        }
    }

    fun sendTestSms() {
        val snapshot = formState.value
        viewModelScope.launch {
            formState.update { it.copy(isSending = true, sendResultMessage = null) }
            when (val result = smsGatewayManager.sendSms(
                recipient = snapshot.recipient,
                message = snapshot.body,
                simSlot = snapshot.selectedSimSlot
            )) {
                is SmsSendResult.Success -> {
                    formState.update {
                        it.copy(
                            isSending = false,
                            body = "",
                            sendResultMessage = "SMS queued with id ${result.messageId.take(8)}"
                        )
                    }
                }

                is SmsSendResult.Failure -> {
                    formState.update {
                        it.copy(
                            isSending = false,
                            sendResultMessage = result.reason
                        )
                    }
                }
            }
        }
    }
}
