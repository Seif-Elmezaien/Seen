package com.example.seen.ui.reminder.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.seen.datasource.repository.MedicineRepository
import com.example.seen.datasource.repository.ReminderRepository
import com.example.seen.domain.model.entites.Medicine
import com.example.seen.domain.model.entites.Reminder
import kotlinx.coroutines.launch


class ReminderViewModel(
    app: Application,
    private val reminderRepository: ReminderRepository,
    private val medicineRepository: MedicineRepository
) : AndroidViewModel(app) {

    suspend fun insertReminder(reminder: Reminder) =
        reminderRepository.insertReminder(reminder)

    fun deleteReminder(reminder: Reminder) = viewModelScope.launch {
        reminderRepository.deleteReminder(reminder)
    }

    fun getAllReminders() =
        reminderRepository.getAllReminders()

    fun getAllMedicines() =
        medicineRepository.getAllMedicines()

    fun insertMedicine(medicine: Medicine) = viewModelScope.launch {
        medicineRepository.insertMedicine(medicine)
    }

}