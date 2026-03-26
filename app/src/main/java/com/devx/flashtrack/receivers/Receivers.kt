package com.devx.flashtrack.receivers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.devx.flashtrack.R

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("title") ?: "FlashTrack Reminder"
        val body  = intent.getStringExtra("body")  ?: "You have a pending reminder"
        val notifId = intent.getIntExtra("notif_id", 0)

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "flashtrack_reminders",
                "FlashTrack Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply { description = "Expense and bill reminders" }
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, "flashtrack_reminders")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        manager.notify(notifId, notification)
    }
}

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Re-schedule all active reminders after reboot
            // Would query DB and re-schedule AlarmManager entries
        }
    }
}
