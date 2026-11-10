package com.meditrack.app

import android.app.Application
import com.meditrack.app.data.MediTrackDatabase
import com.meditrack.app.notification.ExpiryCheckWorker
import com.meditrack.app.notification.NotificationHelper

/**
 * Application class for MedCycle.
 * Initialises notification channels and schedules the daily expiry check job.
 */
class MediTrackApp : Application() {
    val database: MediTrackDatabase by lazy { MediTrackDatabase.getInstance(this) }

    override fun onCreate() {
        super.onCreate()
        // Create notification channels (safe to call multiple times)
        NotificationHelper.createChannel(this)
        NotificationHelper.createExpiryChannel(this)
        // Schedule the daily expiry check WorkManager job
        ExpiryCheckWorker.schedule(this)
    }
}
