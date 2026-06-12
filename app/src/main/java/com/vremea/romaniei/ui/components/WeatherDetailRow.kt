package com.vremea.romaniei.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vremea.romaniei.domain.model.WeatherData

@Composable
fun WeatherDetailRow(weather: WeatherData) {
    val current = weather.current ?: return

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        DetailCard(
            modifier = Modifier.weight(1f),
            label = "Presiune",
            value = "${current.pressure.toInt()} hPa",
            icon = "🌡️"
        )
        DetailCard(
            modifier = Modifier.weight(1f),
            label = "Vânt",
            value = "${current.windSpeed.toInt()} km/h",
            icon = "💨"
        )
    }

    Spacer(modifier = Modifier.height(8.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        DetailCard(
            modifier = Modifier.weight(1f),
            label = "Rafale",
            value = "${current.windGusts.toInt()} km/h",
            icon = "🌬️"
        )
        DetailCard(
            modifier = Modifier.weight(1f),
            label = "UV Index",
            value = "${current.uvIndex.toInt()}",
            icon = "☀️"
        )
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Additional info
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Detalii suplimentare",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Răsărit", style = MaterialTheme.typography.labelSmall)
                    val sunrise = weather.daily.firstOrNull()?.sunrise
                    if (sunrise != null) {
                        Text(
                            text = java.text.SimpleDateFormat(
                                "HH:mm", java.util.Locale.getDefault()
                            ).format(java.util.Date(sunrise)),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Apus", style = MaterialTheme.typography.labelSmall)
                    val sunset = weather.daily.firstOrNull()?.sunset
                    if (sunset != null) {
                        Text(
                            text = java.text.SimpleDateFormat(
                                "HH:mm", java.util.Locale.getDefault()
                            ).format(java.util.Date(sunset)),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    icon: String
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = icon, style = MaterialTheme.typography.titleLarge)
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
