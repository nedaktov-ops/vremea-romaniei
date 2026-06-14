package com.vremea.romaniei.ui.screens.forecast

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vremea.romaniei.R
import com.vremea.romaniei.data.repository.WeatherRepository
import com.vremea.romaniei.domain.model.WeatherData
import com.vremea.romaniei.util.UiText
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ForecastViewModel : ViewModel() {

    private val repository = WeatherRepository()

    private val _forecastState = MutableStateFlow<ForecastUiState>(ForecastUiState.Loading)
    val forecastState: StateFlow<ForecastUiState> = _forecastState.asStateFlow()

    fun loadForecast(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            _forecastState.value = ForecastUiState.Loading
            repository.getForecast(latitude, longitude)
                .onSuccess { data ->
                    _forecastState.value = ForecastUiState.Success(data)
                }
                .onFailure { error ->
                    _forecastState.value = ForecastUiState.Error(
                        UiText.DynamicString(error.message ?: "")
                            .takeIf { error.message != null }
                            ?: UiText.StringResource(R.string.error_message)
                    )
                }
        }
    }
}

sealed class ForecastUiState {
    data object Loading : ForecastUiState()
    data class Success(val data: WeatherData) : ForecastUiState()
    data class Error(val message: UiText) : ForecastUiState()
}
