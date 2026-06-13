package com.vremea.romaniei.ui.screens.map

import android.Manifest
import android.content.pm.PackageManager
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vremea.romaniei.data.location.LocationHelper
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    viewModel: MapViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var mapView by remember { mutableStateOf<org.maplibre.android.maps.MapView?>(null) }

    // Location permission
    val locationPermissionGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
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
                // Keep Romania center default
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hartă Meteorologică") },
                actions = {
                    IconButton(onClick = {
                        if (!locationPermissionGranted) {
                            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                        } else {
                            val helper = LocationHelper(context)
                            helper.getLastLocation()
                                .addOnSuccessListener { location ->
                                    if (location != null) {
                                        viewModel.setCenter(location.latitude, location.longitude)
                                        mapView?.getMapAsync { map ->
                                            map.cameraPosition = org.maplibre.android.camera.CameraPosition.Builder()
                                                .target(org.maplibre.android.geometry.LatLng(
                                                    location.latitude, location.longitude
                                                ))
                                                .zoom(10.0)
                                                .build()
                                        }
                                    }
                                }
                        }
                    }) {
                        Icon(Icons.Default.MyLocation, contentDescription = "My Location")
                    }
                    IconButton(onClick = { viewModel.toggleFullscreen() }) {
                        Icon(Icons.Default.Layers, contentDescription = "Layers")
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
                    org.maplibre.android.MapLibre.getInstance(ctx)
                    org.maplibre.android.maps.MapView(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        onCreate(null)
                        mapView = this
                        getMapAsync { map ->
                            map.setStyle("https://demotiles.maplibre.org/style.json")
                            map.cameraPosition = org.maplibre.android.camera.CameraPosition.Builder()
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
                        Lifecycle.Event.ON_DESTROY -> mapView?.onDestroy()
                        else -> {}
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
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
                    Text("°C", style = MaterialTheme.typography.labelSmall)
                }
                SmallFloatingActionButton(
                    onClick = { viewModel.setLayer("precipitation") },
                    containerColor = if (state.activeLayer == "precipitation")
                        MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.surface
                ) {
                    Text("Plm", style = MaterialTheme.typography.labelSmall)
                }
                SmallFloatingActionButton(
                    onClick = { viewModel.setLayer("wind") },
                    containerColor = if (state.activeLayer == "wind")
                        MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.surface
                ) {
                    Text("Vânt", style = MaterialTheme.typography.labelSmall)
                }
                SmallFloatingActionButton(
                    onClick = { viewModel.setLayer("radar") },
                    containerColor = if (state.activeLayer == "radar")
                        MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.surface
                ) {
                    Text("Rad", style = MaterialTheme.typography.labelSmall)
                }
            }

            // Map attribution
            Text(
                text = "© OpenStreetMap contributors © MapLibre",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(4.dp)
            )
        }
    }
}
