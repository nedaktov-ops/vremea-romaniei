package com.vremea.romaniei.ui.screens.map

import android.Manifest
import android.annotation.SuppressLint
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.vremea.romaniei.R
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.foundation.layout.size
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vremea.romaniei.data.location.LocationHelper
import kotlinx.coroutines.tasks.await
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.style.layers.RasterLayer
import org.maplibre.android.style.sources.RasterSource
import org.maplibre.android.style.sources.TileSet

private const val RADAR_SOURCE_ID = "radar-source"
private const val RADAR_LAYER_ID = "radar-layer"

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("MissingPermission")  // Permission checked at runtime via locationPermissionGranted
@Composable
fun MapScreen(
    viewModel: MapViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var mapView by remember { mutableStateOf<MapView?>(null) }
    var map by remember { mutableStateOf<MapLibreMap?>(null) }

    // Location permission (check both COARSE and FINE)
    val locationPermissionGranted by remember {
        mutableStateOf(LocationHelper.isLocationPermissionGranted(context))
    }
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { _ -> }

    // Try to get user location on first launch
    LaunchedEffect(Unit) {
        if (locationPermissionGranted) {
            val helper = LocationHelper(context)
            try {
                val location = helper.getLastLocation().await()
                if (location != null) {
                    viewModel.setCenter(location.latitude, location.longitude)
                }
            } catch (_: Exception) {
                // Keep default
            }
        }
    }

    // Radar layer management: add/remove/update when state changes
    LaunchedEffect(
        state.activeLayer,
        state.radarCurrentIndex,
        state.radarFrameUrls
    ) {
        val mapLibreMap = map ?: return@LaunchedEffect
        if (state.activeLayer == "radar" && state.radarFrameUrls.isNotEmpty()) {
            val tileUrl = state.radarFrameUrls[state.radarCurrentIndex]
            try {
                // Remove existing radar layer + source
                mapLibreMap.style?.removeLayer(RADAR_LAYER_ID)
                mapLibreMap.style?.removeSource(RADAR_SOURCE_ID)

                // Add new source and layer
                val tileSet = TileSet("tileset", tileUrl)
                tileSet.minZoom = 3f
                tileSet.maxZoom = 16f
                val source = RasterSource(RADAR_SOURCE_ID, tileSet, 256)
                mapLibreMap.style?.addSource(source)

                val layer = RasterLayer(RADAR_LAYER_ID, RADAR_SOURCE_ID)
                // Place radar below labels but above base map
                layer.setProperties(
                    org.maplibre.android.style.layers.PropertyFactory.rasterOpacity(0.65f)
                )
                mapLibreMap.style?.addLayer(layer)
            } catch (_: Exception) {
                // Map style may not be ready yet; skip this frame
            }
        } else if (state.activeLayer != "radar") {
            // Clean up radar layer when switching away
            try {
                mapLibreMap.style?.removeLayer(RADAR_LAYER_ID)
                mapLibreMap.style?.removeSource(RADAR_SOURCE_ID)
            } catch (_: Exception) { }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.weather_map)) },
                actions = {
                    IconButton(onClick = {
                        if (!locationPermissionGranted) {
                            locationPermissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        } else {
                            val helper = LocationHelper(context)
                            helper.getLastLocation()
                                .addOnSuccessListener { location ->
                                    if (location != null) {
                                        viewModel.setCenter(location.latitude, location.longitude)
                                        map?.cameraPosition = org.maplibre.android.camera.CameraPosition.Builder()
                                            .target(org.maplibre.android.geometry.LatLng(
                                                location.latitude, location.longitude
                                            ))
                                            .zoom(10.0)
                                            .build()
                                    }
                                }
                        }
                    }) {
                        Icon(Icons.Default.MyLocation, contentDescription = stringResource(R.string.my_location))
                    }
                    IconButton(onClick = { viewModel.toggleFullscreen() }) {
                        Icon(Icons.Default.Layers, contentDescription = stringResource(R.string.layers))
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // MapLibre GL Map
            AndroidView(
                factory = { ctx ->
                    MapView(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        onCreate(null)
                        mapView = this
                        getMapAsync { mapLibreMap ->
                            map = mapLibreMap
                            mapLibreMap.setStyle("https://tiles.openfreemap.org/styles/liberty")
                            mapLibreMap.cameraPosition = org.maplibre.android.camera.CameraPosition.Builder()
                                .target(org.maplibre.android.geometry.LatLng(
                                    state.centerLat, state.centerLon
                                ))
                                .zoom(state.zoom.toDouble())
                                .build()
                        }
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            DisposableEffect(lifecycleOwner) {
                val observer = LifecycleEventObserver { _, event ->
                    when (event) {
                        Lifecycle.Event.ON_START -> mapView?.onStart()
                        Lifecycle.Event.ON_RESUME -> mapView?.onResume()
                        Lifecycle.Event.ON_PAUSE -> mapView?.onPause()
                        Lifecycle.Event.ON_STOP -> mapView?.onStop()
                        Lifecycle.Event.ON_DESTROY -> {
                            mapView?.onDestroy()
                            mapView = null
                            map = null
                        }
                        else -> {}
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
            }

            // Radar loading indicator
            if (state.isRadarLoading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 16.dp)
                        .size(32.dp)
                )
            }

            // Layer selector FAB
            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SmallFloatingActionButton(
                    onClick = { viewModel.setLayer("temperature") },
                    containerColor = if (state.activeLayer == "temperature")
                        MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.surface
                ) {
                    Text(stringResource(R.string.map_layer_temp_short), style = MaterialTheme.typography.labelSmall)
                }
                SmallFloatingActionButton(
                    onClick = { viewModel.setLayer("precipitation") },
                    containerColor = if (state.activeLayer == "precipitation")
                        MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.surface
                ) {
                    Text(stringResource(R.string.map_layer_precip_short), style = MaterialTheme.typography.labelSmall)
                }
                SmallFloatingActionButton(
                    onClick = { viewModel.setLayer("wind") },
                    containerColor = if (state.activeLayer == "wind")
                        MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.surface
                ) {
                    Text(stringResource(R.string.map_layer_wind_short), style = MaterialTheme.typography.labelSmall)
                }
                SmallFloatingActionButton(
                    onClick = { viewModel.setLayer("radar") },
                    containerColor = if (state.activeLayer == "radar")
                        MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.surface
                ) {
                    Text(stringResource(R.string.map_layer_radar_short), style = MaterialTheme.typography.labelSmall)
                }
            }

            // Map attribution
            Text(
                text = stringResource(R.string.map_attribution),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(4.dp)
            )
        }
    }
}
