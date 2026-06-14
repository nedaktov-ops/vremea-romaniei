package com.vremea.romaniei.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.vremea.romaniei.R
import com.vremea.romaniei.domain.model.AlertData
import com.vremea.romaniei.domain.model.AlertSeverity

object AlertNotificationManager {
    private const val CHANNEL_ID = "vremea_alerts"
    private const val NOTIFICATION_PREFIX = 2000

    fun createNotificationChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID, context.getString(R.string.weather_alerts), NotificationManager.IMPORTANCE_HIGH
        ).apply { enableVibration(true) }
        (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
            .createNotificationChannel(channel)
    }

    fun showAlertNotification(context: Context, alert: AlertData) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) return
        }
        val color = when (alert.severity) {
            AlertSeverity.MINOR -> 0xFF2196F3.toInt()
            AlertSeverity.MODERATE -> 0xFFFFC107.toInt()
            AlertSeverity.SEVERE -> 0xFFFF9800.toInt()
            AlertSeverity.EXTREME -> 0xFFF44336.toInt()
        }
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(alert.title)
            .setContentText(alert.description.take(200))
            .setStyle(NotificationCompat.BigTextStyle().bigText(alert.description))
            .setColor(color).setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .build()
        NotificationManagerCompat.from(context).notify(NOTIFICATION_PREFIX + alert.id.hashCode(), notification)
    }
}
