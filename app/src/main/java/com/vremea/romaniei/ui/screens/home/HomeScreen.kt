package com.vremea.romaniei.ui.screens.home

import android.Manifest
import android.annotation.SuppressLint
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vremea.romaniei.data.location.LocationHelper
import kotlinx.coroutines.tasks.await
import com.vremea.romaniei.ui.components.CurrentWeatherCard
import com.vremea.romaniei.ui.components.HourlyForecastRow
import com.vremea.romaniei.ui.components.WeatherDetailRow

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("MissingPermission")  // Permission checked at runtime via locationPermissionGranted
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = viewModel()
) {
    val weatherState by viewModel.weatherState.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val context = LocalContext.current

    // Location permission state (check both COARSE and FINE)
    var locationPermissionGranted by remember {
        mutableStateOf(LocationHelper.isLocationPermissionGranted(context))
    }

    // Permission request launcher (request both COARSE and FINE)
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { grantedMap ->
        locationPermissionGranted = grantedMap.values.any { it }
    }

    // Location tracking
    var hasRequestedLocation by remember { mutableStateOf(false) }

    // Load weather on first composition
    LaunchedEffect(locationPermissionGranted) {
        if (locationPermissionGranted && !hasRequestedLocation) {
            hasRequestedLocation = true
            val helper = LocationHelper(context)
            try {
                val location = helper.getLastLocation().await()
                if (location != null) {
                    viewModel.loadWeather(location.latitude, location.longitude)
                } else {
                    viewModel.loadWeather(LocationHelper.DEFAULT_LAT, LocationHelper.DEFAULT_LON)
                }
            } catch (_: Exception) {
                viewModel.loadWeather(LocationHelper.DEFAULT_LAT, LocationHelper.DEFAULT_LON)
            }
        } else if (!hasRequestedLocation) {
            hasRequestedLocation = true
            viewModel.loadWeather(LocationHelper.DEFAULT_LAT, LocationHelper.DEFAULT_LON)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "VremeaRomâniei",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                actions = {
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                    IconButton(onClick = { /* TODO: search */ }) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
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
                                        viewModel.loadWeather(location.latitude, location.longitude)
                                    }
                                }
                        }
                    }) {
                        Icon(Icons.Default.MyLocation, contentDescription = "My Location")
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
            when (val state = weatherState) {
                is WeatherUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is WeatherUiState.Error -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = state.message.ifEmpty { "Nu s-au putut încărca datele meteorologice" },
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.refresh() }) {
                            Text("Reîncearcă")
                        }
                    }
                }
                is WeatherUiState.Success -> {
                    val weather = state.data
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    ) {
                        CurrentWeatherCard(weather = weather)
                        Spacer(modifier = Modifier.height(16.dp))
                        HourlyForecastRow(hourlyData = weather.hourly)
                        Spacer(modifier = Modifier.height(16.dp))
                        WeatherDetailRow(weather = weather)
                    }
                }
            }

            if (isRefreshing) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                )
            }
        }
    }
}
