package com.vremea.romaniei.ui.screens.alerts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vremea.romaniei.domain.model.AlertData
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AlertsViewModel : ViewModel() {
    private val _state = MutableStateFlow<AlertsUiState>(AlertsUiState.Loading)
    val state: StateFlow<AlertsUiState> = _state.asStateFlow()

    init { loadAlerts() }

    fun loadAlerts() {
        viewModelScope.launch {
            _state.value = AlertsUiState.Loading
            delay(300)
            _state.value = AlertsUiState.Success(emptyList())
        }
    }
    fun refresh() { loadAlerts() }
}

sealed class AlertsUiState {
    data object Loading : AlertsUiState()
    data class Success(val alerts: List<AlertData>) : AlertsUiState()
    data class Error(val message: String) : AlertsUiState()
}
