package com.vremea.romaniei.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vremea.romaniei.domain.model.AlertData
import com.vremea.romaniei.domain.model.AlertSeverity

@Composable
fun AlertBanner(alert: AlertData) {
    val severityColor = when (alert.severity) {
        AlertSeverity.MINOR -> MaterialTheme.colorScheme.tertiary
        AlertSeverity.MODERATE -> com.vremea.romaniei.ui.theme.AlertYellow
        AlertSeverity.SEVERE -> com.vremea.romaniei.ui.theme.AlertOrange
        AlertSeverity.EXTREME -> com.vremea.romaniei.ui.theme.AlertRed
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = severityColor.copy(alpha = 0.15f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = when (alert.severity) {
                        AlertSeverity.MINOR -> "ℹ️"
                        AlertSeverity.MODERATE -> "⚠️"
                        AlertSeverity.SEVERE -> "🔶"
                        AlertSeverity.EXTREME -> "🔴"
                    },
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = alert.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = alert.eventType,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = alert.description,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 3
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Sursă: ${alert.source}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
