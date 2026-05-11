package com.example.seen.ui.home.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.seen.datasource.repository.ReminderRepository
import com.example.seen.domain.model.entites.Reminder


class ReminderViewModel(
    app: Application,
    private val reminderRepository: ReminderRepository
) : AndroidViewModel(app) {

    suspend fun insertReminder(reminder: Reminder) =
        reminderRepository.insertReminder(reminder)

    suspend fun deleteReminder(reminder: Reminder) =
        reminderRepository.deleteReminder(reminder)

    fun getAllReminders() =
        reminderRepository.getAllReminders()

}