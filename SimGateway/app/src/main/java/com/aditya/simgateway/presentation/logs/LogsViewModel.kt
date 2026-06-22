package com.aditya.simgateway.presentation.logs

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.aditya.simgateway.SimGatewayApplication
import com.aditya.simgateway.core.diagnostics.EventCategory
import com.aditya.simgateway.data.entity.EventLogEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

enum class LogFilter(
    val label: String,
    val category: EventCategory?
) {
    ALL("All", null),
    INFO("Info", EventCategory.INFO),
    WARNING("Warning", EventCategory.WARNING),
    ERROR("Error", EventCategory.ERROR),
    SMS("SMS", EventCategory.SMS),
    NETWORK("Network", EventCategory.NETWORK),
    SYSTEM("System", EventCategory.SYSTEM),
    DEVICE("Device", EventCategory.DEVICE)
}

@OptIn(ExperimentalCoroutinesApi::class)
class LogsViewModel(application: Application) : AndroidViewModel(application) {

    private val eventRepository = (application as SimGatewayApplication)
        .appContainer
        .eventRepository

    private val selectedFilter = MutableStateFlow(LogFilter.ALL)

    val events: StateFlow<List<EventLogEntity>> = selectedFilter
        .flatMapLatest { filter ->
            filter.category?.let(eventRepository::filterEvents) ?: eventRepository.readEvents()
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = emptyList()
        )

    val activeFilter: StateFlow<LogFilter> = selectedFilter

    fun setFilter(filter: LogFilter) {
        selectedFilter.value = filter
    }
}
