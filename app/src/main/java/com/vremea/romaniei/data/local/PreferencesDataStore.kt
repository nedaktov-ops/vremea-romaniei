package com.vremea.romaniei.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class PreferencesDataStore(private val context: Context) {

    companion object {
        val LOCATION_NAME = stringPreferencesKey("location_name")
        val LOCATION_LAT = doublePreferencesKey("location_lat")
        val LOCATION_LON = doublePreferencesKey("location_lon")
        val TEMP_UNIT = stringPreferencesKey("temp_unit")
        val LANGUAGE = stringPreferencesKey("language")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val UPDATE_INTERVAL = intPreferencesKey("update_interval")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
    }

    data class AppSettings(
        val locationName: String = "București",
        val locationLat: Double = 44.4268,
        val locationLon: Double = 26.1025,
        val tempUnit: String = "celsius",
        val language: String = "ro",
        val themeMode: String = "system",
        val updateInterval: Int = 2,
        val notificationsEnabled: Boolean = true
    )

    val settingsFlow: Flow<AppSettings> = context.dataStore.data.map { prefs ->
        AppSettings(
            locationName = prefs[LOCATION_NAME] ?: "București",
            locationLat = prefs[LOCATION_LAT] ?: 44.4268,
            locationLon = prefs[LOCATION_LON] ?: 26.1025,
            tempUnit = prefs[TEMP_UNIT] ?: "celsius",
            language = prefs[LANGUAGE] ?: "ro",
            themeMode = prefs[THEME_MODE] ?: "system",
            updateInterval = prefs[UPDATE_INTERVAL] ?: 2,
            notificationsEnabled = prefs[NOTIFICATIONS_ENABLED] ?: true
        )
    }

    suspend fun updateLocation(name: String, lat: Double, lon: Double) {
        context.dataStore.edit { prefs ->
            prefs[LOCATION_NAME] = name
            prefs[LOCATION_LAT] = lat
            prefs[LOCATION_LON] = lon
        }
    }

    suspend fun updateLanguage(language: String) {
        context.dataStore.edit { prefs ->
            prefs[LANGUAGE] = language
        }
    }

    suspend fun updateThemeMode(mode: String) {
        context.dataStore.edit { prefs ->
            prefs[THEME_MODE] = mode
        }
    }

    suspend fun updateNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[NOTIFICATIONS_ENABLED] = enabled
        }
    }

    suspend fun updateUpdateInterval(hours: Int) {
        context.dataStore.edit { prefs ->
            prefs[UPDATE_INTERVAL] = hours
        }
    }

}
