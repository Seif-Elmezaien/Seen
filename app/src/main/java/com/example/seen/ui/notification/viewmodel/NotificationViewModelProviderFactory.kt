package com.example.seen.ui.notification.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.seen.datasource.repository.AuthRepository
import com.example.seen.datasource.repository.LogRepository
import com.example.seen.datasource.repository.NotificationRepository
import com.example.seen.datasource.repository.UserRepository
import com.example.seen.ui.authentication.viewmodel.AuthViewModel
import com.example.seen.ui.home.viewmodel.HomeViewModel

class NotificationViewModelProviderFactory(
    val app: Application,
    val notificationRepository: NotificationRepository,
): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return NotificationViewModel(app, notificationRepository) as T
    }
}