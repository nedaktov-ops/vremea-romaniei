package com.vremea.romaniei.data.local

import android.content.Context
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters

class AlertWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        return try {
            setForeground(createForegroundInfo())
            Log.d(TAG, "Alert check worker executing")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Alert check failed", e)
            Result.retry()
        }
    }
    override suspend fun getForegroundInfo(): ForegroundInfo = createForegroundInfo()
    private fun createForegroundInfo(): ForegroundInfo {
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setContentTitle("VremeaRomâniei")
            .setContentText("Verificare alerte meteo...")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setOngoing(true).setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
        return ForegroundInfo(NOTIFICATION_ID, notification)
    }
    companion object {
        private const val TAG = "AlertWorker"
        private const val CHANNEL_ID = "vremea_alert_check"
        private const val NOTIFICATION_ID = 1002
    }
}
