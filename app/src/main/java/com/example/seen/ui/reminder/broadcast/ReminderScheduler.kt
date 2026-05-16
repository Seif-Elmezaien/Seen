package com.example.seen.ui.reminder.broadcast

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build

object ReminderScheduler {

    fun scheduleReminder(
        context: Context,
        reminderId: Int,
        logType: String,
        title: String,
        time: Long,
        medicationName: String?
    ) {

        val intent = Intent(context, ReminderReceiver::class.java)

        intent.putExtra("reminder_type", logType)
        intent.putExtra("title", title)
        intent.putExtra("medication_name", medicationName)


        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminderId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or
                    PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager =
            context.getSystemService(Context.ALARM_SERVICE)
                    as AlarmManager

        try {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {

                if (alarmManager.canScheduleExactAlarms()) {

                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        time,
                        pendingIntent
                    )
                }

            } else {

                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    time,
                    pendingIntent
                )
            }

        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    fun cancelReminder(
        context: Context,
        reminderId: Int
    ) {

        val intent = Intent(context, ReminderReceiver::class.java)

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminderId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager =
            context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        alarmManager.cancel(pendingIntent)

        pendingIntent.cancel()
    }
}