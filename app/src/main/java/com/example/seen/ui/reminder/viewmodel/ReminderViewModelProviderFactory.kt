package com.example.seen.ui.reminder.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.seen.datasource.repository.MedicineRepository
import com.example.seen.datasource.repository.ReminderRepository

class ReminderViewModelProviderFactory(
    val app: Application,
    val reminderRepository: ReminderRepository,
    val medicineRepository: MedicineRepository
): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ReminderViewModel(app, reminderRepository, medicineRepository) as T
    }
}