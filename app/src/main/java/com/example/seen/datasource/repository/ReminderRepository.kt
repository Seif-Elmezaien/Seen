package com.example.seen.datasource.repository

import com.example.seen.datasource.local.SeenDatabase
import com.example.seen.domain.model.entites.Log
import com.example.seen.domain.model.entites.RecordGlucose
import com.example.seen.domain.model.entites.RecordMeal
import com.example.seen.domain.model.entites.RecordMedication
import com.example.seen.domain.model.entites.Reminder
import com.example.seen.domain.model.entites.User

class ReminderRepository(
    val db : SeenDatabase
) {
    suspend fun insertReminder(reminder: Reminder) =
        db.reminderDao.insertReminder(reminder)

    suspend fun deleteReminder(reminder: Reminder) =
        db.reminderDao.deleteReminder(reminder)

    fun getAllReminders() =
        db.reminderDao.getAllReminders()

}