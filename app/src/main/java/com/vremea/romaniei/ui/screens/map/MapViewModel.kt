package com.vremea.romaniei.ui.screens.map

import androidx.lifecycle.ViewModel
import com.vremea.romaniei.data.location.LocationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class MapUiState(
    val centerLat: Double = LocationHelper.ROMANIA_CENTER_LAT,
    val centerLon: Double = LocationHelper.ROMANIA_CENTER_LON,
    val zoom: Float = 6.5f,
    val activeLayer: String = "temperature",
    val isFullscreen: Boolean = false
)

class MapViewModel : ViewModel() {
    private val _state = MutableStateFlow(MapUiState())
    val state: StateFlow<MapUiState> = _state.asStateFlow()

    fun setCenter(latitude: Double, longitude: Double) {
        _state.value = _state.value.copy(
            centerLat = latitude,
            centerLon = longitude,
            zoom = 10f // Closer zoom when user location is known
        )
    }

    fun setLayer(layer: String) {
        _state.value = _state.value.copy(activeLayer = layer)
    }

    fun toggleFullscreen() {
        _state.value = _state.value.copy(isFullscreen = !_state.value.isFullscreen)
    }
}
