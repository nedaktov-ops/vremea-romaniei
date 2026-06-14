package com.vremea.romaniei.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vremea.romaniei.R
import com.vremea.romaniei.data.repository.WeatherRepository
import com.vremea.romaniei.domain.model.WeatherData
import com.vremea.romaniei.util.UiText
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    private val repository = WeatherRepository()

    private val _weatherState = MutableStateFlow<WeatherUiState>(WeatherUiState.Loading)
    val weatherState: StateFlow<WeatherUiState> = _weatherState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private var currentLat = 44.4268
    private var currentLon = 26.1025

    fun loadWeather(latitude: Double, longitude: Double) {
        currentLat = latitude
        currentLon = longitude
        viewModelScope.launch {
            _weatherState.value = WeatherUiState.Loading
            repository.getForecast(latitude, longitude)
                .onSuccess { data ->
                    _weatherState.value = WeatherUiState.Success(data)
                }
                .onFailure { error ->
                    _weatherState.value = WeatherUiState.Error(
                        UiText.DynamicString(error.message ?: "")
                            .takeIf { error.message != null }
                            ?: UiText.StringResource(R.string.error_message)
                    )
                }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            repository.getForecast(currentLat, currentLon)
                .onSuccess { data ->
                    _weatherState.value = WeatherUiState.Success(data)
                }
            _isRefreshing.value = false
        }
    }
}

sealed class WeatherUiState {
    data object Loading : WeatherUiState()
    data class Success(val data: WeatherData) : WeatherUiState()
    data class Error(val message: UiText) : WeatherUiState()
}
