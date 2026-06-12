package com.vremea.romaniei.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vremea.romaniei.domain.model.HourlyWeather
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HourlyForecastRow(hourlyData: List<HourlyWeather>) {
    val now = System.currentTimeMillis()
    val next24h = hourlyData.filter { it.time >= now }.take(24)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Următoarele 24 de ore",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(12.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(next24h) { hour ->
                    HourlyItem(hour = hour)
                }
            }
        }
    }
}

@Composable
private fun HourlyItem(hour: HourlyWeather) {
    val sdf = remember { SimpleDateFormat("HH", Locale.getDefault()) }

    Column(
        modifier = Modifier
            .width(48.dp)
            .padding(horizontal = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = sdf.format(Date(hour.time)),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = when (hour.weatherIcon) {
                "sunny", "mostly_sunny" -> "☀️"
                "partly_cloudy" -> "⛅"
                "overcast" -> "☁️"
                "foggy" -> "🌫️"
                "drizzle", "rainy" -> "🌧️"
                "snowy" -> "❄️"
                "thunderstorm" -> "⛈️"
                else -> "🌤️"
            },
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "${hour.temperature.toInt()}°",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold
        )

        if (hour.precipitationProbability > 0) {
            Text(
                text = "${hour.precipitationProbability}%",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
