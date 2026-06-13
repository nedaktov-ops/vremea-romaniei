package com.vremea.romaniei

import android.app.Application
import androidx.work.Configuration
import androidx.work.WorkManager
import com.vremea.romaniei.data.local.BootReceiver

class VremeaApp : Application() {
    override fun onCreate() {
        super.onCreate()
        instance = this
        val config = Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()
        WorkManager.initialize(this, config)
        BootReceiver.scheduleWorkers(this)
    }

    companion object {
        @Volatile
        private var instance: VremeaApp? = null

        fun getInstance(): VremeaApp {
            return instance ?: throw IllegalStateException("VremeaApp not initialized")
        }
    }
}
