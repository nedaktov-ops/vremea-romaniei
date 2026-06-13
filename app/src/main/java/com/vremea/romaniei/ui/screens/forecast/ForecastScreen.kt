package com.vremea.romaniei.ui.screens.forecast

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vremea.romaniei.data.location.LocationHelper
import kotlinx.coroutines.tasks.await
import com.vremea.romaniei.ui.components.DailyForecastCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForecastScreen(
    viewModel: ForecastViewModel = viewModel()
) {
    val forecastState by viewModel.forecastState.collectAsState()
    val context = LocalContext.current

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

    var hasRequestedLocation by remember { mutableStateOf(false) }

    LaunchedEffect(locationPermissionGranted) {
        if (!hasRequestedLocation) {
            hasRequestedLocation = true
            if (locationPermissionGranted) {
                val helper = LocationHelper(context)
                try {
                    val location = helper.getLastLocation().await()
                    if (location != null) {
                        viewModel.loadForecast(location.latitude, location.longitude)
                    } else {
                        viewModel.loadForecast(LocationHelper.DEFAULT_LAT, LocationHelper.DEFAULT_LON)
                    }
                } catch (_: Exception) {
                    viewModel.loadForecast(LocationHelper.DEFAULT_LAT, LocationHelper.DEFAULT_LON)
                }
            } else {
                viewModel.loadForecast(LocationHelper.DEFAULT_LAT, LocationHelper.DEFAULT_LON)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Prognoză 16 Zile") },
                actions = {
                    IconButton(onClick = {
                        if (!locationPermissionGranted) {
                            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                        } else {
                            val helper = LocationHelper(context)
                            helper.getLastLocation()
                                .addOnSuccessListener { location ->
                                    if (location != null) {
                                        viewModel.loadForecast(location.latitude, location.longitude)
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
            when (val state = forecastState) {
                is ForecastUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is ForecastUiState.Error -> {
                    Text(
                        text = state.message,
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                }
                is ForecastUiState.Success -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    ) {
                        state.data.daily.forEach { day ->
                            DailyForecastCard(day = day)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
    }
}
