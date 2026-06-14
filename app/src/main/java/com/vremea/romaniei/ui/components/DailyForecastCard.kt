package com.vremea.romaniei.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vremea.romaniei.R
import com.vremea.romaniei.domain.model.DailyWeather
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DailyForecastCard(day: DailyWeather) {
    val currentLocale = LocalConfiguration.current.locales[0]
    val daySdf = remember { SimpleDateFormat("EEEE", currentLocale) }
    val dateSdf = remember { SimpleDateFormat("d MMM", currentLocale) }

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
            val weatherIconCd = when (day.weatherIcon) {
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
                    .clearAndSetSemantics { contentDescription = weatherIconCd }
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
