package com.vremea.romaniei.data.local

import android.content.Context
import android.content.Intent
import android.content.BroadcastReceiver
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            scheduleWorkers(context)
        }
    }

    companion object {
        fun scheduleWorkers(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val weatherRequest = PeriodicWorkRequestBuilder<WeatherUpdateWorker>(2, TimeUnit.HOURS)
                .setConstraints(constraints).build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "weather_update", ExistingPeriodicWorkPolicy.KEEP, weatherRequest
            )

            val alertRequest = PeriodicWorkRequestBuilder<AlertWorker>(2, TimeUnit.HOURS)
                .setConstraints(constraints).build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "alert_check", ExistingPeriodicWorkPolicy.KEEP, alertRequest
            )
        }
    }
}
