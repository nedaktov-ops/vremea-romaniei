package com.vremea.romaniei.ui.screens.alerts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vremea.romaniei.data.repository.AlertRepository
import com.vremea.romaniei.domain.model.AlertData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AlertsViewModel : ViewModel() {

    private val repository = AlertRepository()

    private val _state = MutableStateFlow<AlertsUiState>(AlertsUiState.Loading)
    val state: StateFlow<AlertsUiState> = _state.asStateFlow()

    init { loadAlerts() }

    fun loadAlerts() {
        viewModelScope.launch {
            _state.value = AlertsUiState.Loading
            repository.getAlerts()
                .let { alerts ->
                    if (alerts.isEmpty()) {
                        _state.value = AlertsUiState.Success(emptyList())
                    } else {
                        // Sort by severity (most severe first), then by start time
                        val sorted = alerts.sortedWith(
                            compareByDescending<AlertData> { it.severity.ordinal }
                                .thenByDescending { it.startTime }
                        )
                        _state.value = AlertsUiState.Success(sorted)
                    }
                }
        }
    }

    fun refresh() { loadAlerts() }
}

sealed class AlertsUiState {
    data object Loading : AlertsUiState()
    data class Success(val alerts: List<AlertData>) : AlertsUiState()
    data class Error(val message: String) : AlertsUiState()
}
