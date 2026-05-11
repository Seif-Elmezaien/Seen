package com.example.seen.ui.reminder.broadcast

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.seen.R
import com.example.seen.ui.activites.MainActivity

class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        val type = intent.getStringExtra("reminder_type")

        val icon = when (type) {
            "medication" -> R.drawable.ic_medication_notification
            "glucose" -> R.drawable.ic_glucose_notfication
            "meal" -> R.drawable.ic_meal_notification
            else -> R.drawable.ic_notification
        }

        val title = intent.getStringExtra("title") ?: "Reminder"

        val medicationName = intent.getStringExtra("medication_name")

        val contentText = if (!medicationName.isNullOrEmpty()) {
            title + "\n" + context.getString(R.string.medicine_name_notification) + " " + medicationName
        } else {
            title
        }

        val manager =
            context.getSystemService(Context.NOTIFICATION_SERVICE)
                    as NotificationManager

        val channelId = "reminder_channel"

        // create channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val channel = NotificationChannel(
                channelId,
                "Reminder Notifications",
                NotificationManager.IMPORTANCE_HIGH
            )

            manager.createNotificationChannel(channel)
        }

        val openIntent = Intent(context, MainActivity::class.java)

        val openPendingIntent = PendingIntent.getActivity(
            context,
            0,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(icon)
            .setColor(ContextCompat.getColor(context, R.color.primary))
            .setColorized(true)
            .setContentTitle(context.getString(R.string.seen_reminder))
            .setContentText(contentText)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(contentText)
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(openPendingIntent)
            .setAutoCancel(true)
            .build()

        manager.notify(System.currentTimeMillis().toInt(), notification)
    }
}