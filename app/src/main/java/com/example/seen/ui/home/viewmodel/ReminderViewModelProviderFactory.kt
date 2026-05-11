package com.example.seen.ui.home.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.seen.datasource.repository.AuthRepository
import com.example.seen.datasource.repository.LogRepository
import com.example.seen.datasource.repository.ReminderRepository
import com.example.seen.datasource.repository.UserRepository
import com.example.seen.ui.authentication.viewmodel.AuthViewModel

class ReminderViewModelProviderFactory(
    val app: Application,
    val reminderRepository: ReminderRepository,
): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ReminderViewModel(app, reminderRepository) as T
    }
}