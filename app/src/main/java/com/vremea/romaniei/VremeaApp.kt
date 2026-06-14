package com.vremea.romaniei

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import androidx.work.Configuration
import androidx.work.WorkManager
import com.vremea.romaniei.data.local.BootReceiver
import com.vremea.romaniei.notification.AlertNotificationManager
import org.maplibre.android.MapLibre

class VremeaApp : Application() {
    override fun onCreate() {
        super.onCreate()
        instance = this

        // MapLibre init (Application context — best practice, idempotent)
        MapLibre.getInstance(this)

        // WorkManager config
        val config = Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()
        WorkManager.initialize(this, config)
        BootReceiver.scheduleWorkers(this)

        // Create all notification channels at app start (idempotent)
        createAllNotificationChannels()
    }

    private fun createAllNotificationChannels() {
        // High-priority alert channel
        AlertNotificationManager.createNotificationChannel(this)

        // Foreground service channels (low priority)
        createChannel(
            "vremea_alert_check",
            getString(R.string.channel_alert_check_name),
            getString(R.string.channel_alert_check_desc),
            NotificationManager.IMPORTANCE_LOW
        )
        createChannel(
            "vremea_weather_updates",
            getString(R.string.channel_weather_update_name),
            getString(R.string.channel_weather_update_desc),
            NotificationManager.IMPORTANCE_LOW
        )
    }

    private fun createChannel(id: String, name: String, description: String, importance: Int) {
        val channel = NotificationChannel(id, name, importance).apply {
            this.description = description
        }
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    companion object {
        @Volatile
        private var instance: VremeaApp? = null

        fun getInstance(): VremeaApp {
            return instance ?: throw IllegalStateException("VremeaApp not initialized")
        }
    }
}
