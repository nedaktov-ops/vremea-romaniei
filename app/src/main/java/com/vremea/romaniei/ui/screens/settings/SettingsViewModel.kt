package com.vremea.romaniei.ui.screens.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.vremea.romaniei.data.local.PreferencesDataStore
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = PreferencesDataStore(application)

    val settings: StateFlow<PreferencesDataStore.AppSettings> = prefs.settingsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PreferencesDataStore.AppSettings())

    fun updateLanguage(language: String) { viewModelScope.launch { prefs.updateLanguage(language) } }
    fun updateThemeMode(mode: String) { viewModelScope.launch { prefs.updateThemeMode(mode) } }
    fun updateNotificationsEnabled(enabled: Boolean) { viewModelScope.launch { prefs.updateNotificationsEnabled(enabled) } }
    fun updateUpdateInterval(hours: Int) { viewModelScope.launch { prefs.updateUpdateInterval(hours) } }
}
