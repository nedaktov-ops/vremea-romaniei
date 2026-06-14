package com.vremea.romaniei.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription

import com.vremea.romaniei.R
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vremea.romaniei.domain.model.WeatherData
import com.vremea.romaniei.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CurrentWeatherCard(weather: WeatherData) {
    val current = weather.current ?: return

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Weather icon placeholder
            val weatherIconCd = when (current.weatherIcon) {
                "sunny", "mostly_sunny" -> stringResource(R.string.cd_clear_sky)
                "partly_cloudy" -> stringResource(R.string.cd_partly_cloudy)
                "overcast" -> stringResource(R.string.cd_overcast)
                "foggy" -> stringResource(R.string.cd_foggy)
                "drizzle", "rainy" -> stringResource(R.string.cd_rain)
                "snowy" -> stringResource(R.string.cd_snow)
                "thunderstorm" -> stringResource(R.string.cd_thunderstorm)
                else -> stringResource(R.string.cd_fair_weather)
            }
            Text(
                text = when (current.weatherIcon) {
                    "sunny", "mostly_sunny" -> "☀️"
                    "partly_cloudy" -> "⛅"
                    "overcast" -> "☁️"
                    "foggy" -> "🌫️"
                    "drizzle", "rainy" -> "🌧️"
                    "snowy" -> "❄️"
                    "thunderstorm" -> "⛈️"
                    else -> "🌤️"
                },
                style = MaterialTheme.typography.displayLarge,
                modifier = Modifier.clearAndSetSemantics { contentDescription = weatherIconCd }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Temperature
            Text(
                text = "${current.temperature.toInt()}°C",
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Feels like
            Text(
                text = "${stringResource(R.string.feels_like)} ${current.feelsLike.toInt()}°C",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Description
            Text(
                text = current.weatherDescription,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Detail chips row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                WeatherChip(emoji = "💧", value = "${current.humidity}%", label = stringResource(R.string.humidity))
                WeatherChip(emoji = "💨", value = "${current.windSpeed.toInt()} km/h", label = stringResource(R.string.wind))
                WeatherChip(emoji = "☁️", value = "${current.cloudCover}%", label = stringResource(R.string.cloud_cover))
                WeatherChip(emoji = "👁️", value = "${(current.visibility / 1000).toInt()} km", label = stringResource(R.string.visibility))
            }
        }
    }
}

@Composable
private fun WeatherChip(emoji: String, value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = emoji, style = MaterialTheme.typography.titleLarge)
        Text(
            text = value,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
