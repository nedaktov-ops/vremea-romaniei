package com.vremea.romaniei

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.vremea.romaniei.data.local.PreferencesDataStore
import com.vremea.romaniei.ui.navigation.AppNavigation
import com.vremea.romaniei.ui.theme.VremeaRomanieiTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.Locale

class MainActivity : ComponentActivity() {

    private var currentLanguage: String = "ro"

    override fun attachBaseContext(newBase: Context) {
        val lang = try {
            runBlocking {
                val prefs = PreferencesDataStore(newBase)
                prefs.settingsFlow.first().language
            }
        } catch (_: Exception) {
            "ro"
        }
        currentLanguage = lang
        val locale = Locale(lang)
        Locale.setDefault(locale)
        val config = Configuration(newBase.resources.configuration)
        config.setLocale(locale)
        super.attachBaseContext(newBase.createConfigurationContext(config))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Observe language changes and recreate to apply new locale
        lifecycleScope.launch {
            val prefs = PreferencesDataStore(applicationContext)
            prefs.settingsFlow.drop(1).collect { settings ->
                if (settings.language != currentLanguage) {
                    currentLanguage = settings.language
                    recreate()
                }
            }
        }

        setContent {
            VremeaRomanieiTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}
