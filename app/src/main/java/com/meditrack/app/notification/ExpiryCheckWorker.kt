package com.meditrack.app.notification

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.meditrack.app.data.MediTrackRepository
import com.meditrack.app.util.DateTimeUtils
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

/**
 * WorkManager periodic job that runs once daily.
 * It checks every medicine's expiryDate and fires a notification if the medicine
 * is expired or expiring within the user-configured warning threshold.
 */
class ExpiryCheckWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        private const val TAG = "ExpiryCheckWorker"
        private const val WORK_NAME = "expiry_check_daily"

        /**
         * Enqueues the daily expiry check job.
         * Uses KEEP policy so re-enqueue calls don't reset the timer.
         */
        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<ExpiryCheckWorker>(1, TimeUnit.DAYS)
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
            Log.d(TAG, "Expiry check job scheduled")
        }
    }

    override suspend fun doWork(): Result {
        val repository = MediTrackRepository.getInstance(context)
        val medicines = repository.getAllMedicinesList()
        val today = DateTimeUtils.todayIso()
        // Read warning threshold from settings; default 30 days
        val warningDays = repository.expiryWarningDays.first()

        for (medicine in medicines) {
            val expiry = medicine.expiryDate ?: continue
            val daysUntilExpiry = DateTimeUtils.daysBetween(today, expiry)
            if (daysUntilExpiry <= warningDays) {
                NotificationHelper.showExpiryWarning(
                    context = context,
                    notificationId = medicine.id.toInt(),
                    medicineName = medicine.name,
                    daysUntilExpiry = daysUntilExpiry
                )
                Log.d(TAG, "Expiry alert fired for ${medicine.name}: $daysUntilExpiry days")
            }
        }
        return Result.success()
    }
}
