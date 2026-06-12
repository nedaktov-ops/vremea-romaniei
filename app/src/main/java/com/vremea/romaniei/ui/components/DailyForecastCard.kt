package com.vremea.romaniei.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vremea.romaniei.domain.model.DailyWeather
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DailyForecastCard(day: DailyWeather) {
    val daySdf = remember { SimpleDateFormat("EEEE", Locale("ro")) }
    val dateSdf = remember { SimpleDateFormat("d MMM", Locale("ro")) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Day info
            Column(modifier = Modifier.width(100.dp)) {
                Text(
                    text = daySdf.format(Date(day.date)).replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = dateSdf.format(Date(day.date)),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Weather icon
            Text(
                text = when (day.weatherIcon) {
                    "sunny" -> "☀️"
                    "mostly_sunny" -> "🌤️"
                    "partly_cloudy" -> "⛅"
                    "overcast" -> "☁️"
                    "foggy" -> "🌫️"
                    "drizzle", "rainy" -> "🌧️"
                    "snowy" -> "❄️"
                    "thunderstorm" -> "⛈️"
                    else -> "🌤️"
                },
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            // Description
            Text(
                text = day.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )

            // Temperature range
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${day.tempMax.toInt()}°",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${day.tempMin.toInt()}°",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Precipitation probability
            if (day.precipitationProbabilityMax > 0) {
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${day.precipitationProbabilityMax}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
