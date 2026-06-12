package com.vremea.romaniei.ui.screens.map

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class MapUiState(
    val centerLat: Double = 45.9432,
    val centerLon: Double = 24.9668,
    val zoom: Float = 6.5f,
    val activeLayer: String = "temperature",
    val isFullscreen: Boolean = false
)

class MapViewModel : ViewModel() {
    private val _state = MutableStateFlow(MapUiState())
    val state: StateFlow<MapUiState> = _state.asStateFlow()

    fun setLayer(layer: String) {
        _state.value = _state.value.copy(activeLayer = layer)
    }

    fun toggleFullscreen() {
        _state.value = _state.value.copy(isFullscreen = !_state.value.isFullscreen)
    }
}
