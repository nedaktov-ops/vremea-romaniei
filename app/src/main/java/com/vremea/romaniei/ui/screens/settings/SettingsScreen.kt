package com.vremea.romaniei.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = viewModel()
) {
    val settings by viewModel.settings.collectAsState()
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Setări") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Language
            SettingsItem(
                icon = Icons.Default.Language,
                title = "Limbă",
                subtitle = if (settings.language == "ro") "Română" else "English",
                onClick = { showLanguageDialog = true }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            // Temperature Unit
            SettingsItem(
                icon = Icons.Default.Thermostat,
                title = "Unitate Temperatură",
                subtitle = if (settings.tempUnit == "celsius") "Celsius (°C)" else "Fahrenheit (°F)",
                onClick = { }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            // Theme
            SettingsItem(
                icon = Icons.Default.DarkMode,
                title = "Temă",
                subtitle = when (settings.themeMode) {
                    "light" -> "Luminosă"
                    "dark" -> "Întunecată"
                    else -> "Sistem"
                },
                onClick = { showThemeDialog = true }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            // Notifications
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Notifications,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Notificări",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "Alerte meteo și actualizări",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = settings.notificationsEnabled,
                    onCheckedChange = { viewModel.updateNotificationsEnabled(it) }
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            // Update Interval
            SettingsItem(
                icon = Icons.Default.Update,
                title = "Interval Actualizare",
                subtitle = "La fiecare ${settings.updateInterval} ore",
                onClick = {
                    val newInterval = when (settings.updateInterval) {
                        1 -> 2; 2 -> 4; 4 -> 6; else -> 1
                    }
                    viewModel.updateUpdateInterval(newInterval)
                }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            // About section
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Despre",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            SettingsItem(
                icon = Icons.Default.Info,
                title = "Versiune",
                subtitle = "1.0.0",
                onClick = {}
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            SettingsItem(
                icon = Icons.Default.OpenInNew,
                title = "Surse de Date",
                subtitle = "Open-Meteo, ANM, MeteoAlarm EU",
                onClick = {}
            )
        }
    }

    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text("Selectează Limba") },
            text = {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.updateLanguage("ro")
                                showLanguageDialog = false
                            }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Română 🇷🇴", style = MaterialTheme.typography.bodyLarge)
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.updateLanguage("en")
                                showLanguageDialog = false
                            }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("English 🇬🇧", style = MaterialTheme.typography.bodyLarge)
                    }
                }
            },
            confirmButton = {}
        )
    }

    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text("Selectează Tema") },
            text = {
                Column {
                    listOf("system" to "Sistem", "light" to "Luminosă", "dark" to "Întunecată").forEach { (key, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.updateThemeMode(key)
                                    showThemeDialog = false
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(label, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }
}

@Composable
private fun SettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
