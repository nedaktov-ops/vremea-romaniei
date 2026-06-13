package com.vremea.romaniei.ui.screens.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vremea.romaniei.data.location.LocationHelper
import com.vremea.romaniei.data.repository.RainViewerRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class MapUiState(
    val centerLat: Double = LocationHelper.ROMANIA_CENTER_LAT,
    val centerLon: Double = LocationHelper.ROMANIA_CENTER_LON,
    val zoom: Float = 6.5f,
    val activeLayer: String = "temperature",
    val isFullscreen: Boolean = false,
    // Radar animation state
    val radarFrameUrls: List<String> = emptyList(),
    val radarCurrentIndex: Int = 0,
    val isRadarLoading: Boolean = false
)

class MapViewModel : ViewModel() {

    private val rainViewerRepo = RainViewerRepository()
    private val _state = MutableStateFlow(MapUiState())
    val state: StateFlow<MapUiState> = _state.asStateFlow()

    private var animationJob: Job? = null
    private val FRAME_INTERVAL_MS = 1500L // 1.5 seconds per frame

    fun setCenter(latitude: Double, longitude: Double) {
        _state.value = _state.value.copy(
            centerLat = latitude,
            centerLon = longitude,
            zoom = 10f
        )
    }

    fun setLayer(layer: String) {
        val prevLayer = _state.value.activeLayer
        _state.value = _state.value.copy(activeLayer = layer)

        // Stop radar animation if leaving radar layer
        if (prevLayer == "radar" && layer != "radar") {
            stopRadarAnimation()
        }

        // Start loading radar if entering radar layer
        if (layer == "radar" && prevLayer != "radar") {
            loadRadarFrames()
        }
    }

    fun toggleFullscreen() {
        _state.value = _state.value.copy(isFullscreen = !_state.value.isFullscreen)
    }

    fun loadRadarFrames() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isRadarLoading = true)
            try {
                val wrapper = rainViewerRepo.getFrames()
                if (wrapper.allFrames.isEmpty()) {
                    _state.value = _state.value.copy(isRadarLoading = false)
                    return@launch
                }
                // Build tile URLs for all frames
                val urls = wrapper.allFrames.map { frame ->
                    rainViewerRepo.buildTileUrl(wrapper.host, frame.path)
                }
                _state.value = _state.value.copy(
                    radarFrameUrls = urls,
                    radarCurrentIndex = urls.lastIndex, // Start at most recent
                    isRadarLoading = false
                )
                startRadarAnimation()
            } catch (_: Exception) {
                _state.value = _state.value.copy(isRadarLoading = false)
            }
        }
    }

    private fun startRadarAnimation() {
        animationJob?.cancel()
        animationJob = viewModelScope.launch {
            while (true) {
                delay(FRAME_INTERVAL_MS)
                val urls = _state.value.radarFrameUrls
                if (urls.isEmpty()) break
                val nextIndex = (_state.value.radarCurrentIndex + 1) % urls.size
                _state.value = _state.value.copy(radarCurrentIndex = nextIndex)
            }
        }
    }

    private fun stopRadarAnimation() {
        animationJob?.cancel()
        animationJob = null
    }

    override fun onCleared() {
        super.onCleared()
        stopRadarAnimation()
    }
}
