package com.meditrack.app.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.meditrack.app.MainActivity
import com.meditrack.app.R

/**
 * Helper for creating and managing notification channels and notifications.
 */
object NotificationHelper {

    const val CHANNEL_ID = "medicine_reminders"
    const val CHANNEL_NAME = "Medicine Reminders"

    const val EXPIRY_CHANNEL_ID = "expiry_alerts"
    const val EXPIRY_CHANNEL_NAME = "Expiry Alerts"

    /**
     * Creates the reminder notification channel. Safe to call multiple times.
     */
    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminders to take your medicines on time"
                enableVibration(true)
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    /**
     * Creates the expiry alerts notification channel. Safe to call multiple times.
     */
    fun createExpiryChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                EXPIRY_CHANNEL_ID,
                EXPIRY_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Alerts when medicines are nearing their expiry date or have expired"
                enableVibration(false)
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    /**
     * Builds and shows a medication reminder notification.
     */
    fun showReminder(
        context: Context,
        notificationId: Int,
        medicineName: String,
        dosage: String,
        reminderTime: String
    ) {
        createChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_medication)
            .setContentTitle("Time to take $medicineName")
            .setContentText("$dosage — Scheduled at $reminderTime")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(notificationId, notification)
    }

    /**
     * Shows an expiry warning notification for a medicine expiring soon.
     */
    fun showExpiryWarning(
        context: Context,
        notificationId: Int,
        medicineName: String,
        daysUntilExpiry: Int
    ) {
        createExpiryChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId + 50000,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val (title, body) = if (daysUntilExpiry <= 0) {
            "⚠️ Medicine Expired" to "$medicineName has expired. Please dispose of it safely."
        } else {
            "⏰ Medicine Expiring Soon" to "$medicineName expires in $daysUntilExpiry day(s). Check your cabinet."
        }

        val notification = NotificationCompat.Builder(context, EXPIRY_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_medication)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(notificationId + 50000, notification)
    }
}
