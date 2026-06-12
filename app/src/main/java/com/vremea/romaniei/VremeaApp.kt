package com.vremea.romaniei

import android.app.Application
import androidx.work.Configuration
import androidx.work.WorkManager
import com.vremea.romaniei.data.local.BootReceiver

class VremeaApp : Application(), Configuration.Provider {
    override fun onCreate() {
        super.onCreate()
        WorkManager.initialize(this, workManagerConfiguration)
        BootReceiver.scheduleWorkers(this)
    }
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder().setMinimumLoggingLevel(android.util.Log.INFO).build()
}
