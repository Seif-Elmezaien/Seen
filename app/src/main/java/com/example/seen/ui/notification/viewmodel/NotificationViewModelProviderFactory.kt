package com.example.seen.ui.notification.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.seen.datasource.repository.AuthRepository
import com.example.seen.datasource.repository.CommunityRepository
import com.example.seen.datasource.repository.LogRepository
import com.example.seen.datasource.repository.NotificationRepository
import com.example.seen.datasource.repository.ProfileRepository
import com.example.seen.datasource.repository.UserRepository
import com.example.seen.ui.authentication.viewmodel.AuthViewModel
import com.example.seen.ui.home.viewmodel.HomeViewModel

class NotificationViewModelProviderFactory(
    val app: Application,
    val notificationRepository: NotificationRepository,
    private val profileRepository: ProfileRepository,
    private val communityRepository: CommunityRepository
): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return NotificationViewModel(app, notificationRepository, profileRepository, communityRepository) as T
    }
}