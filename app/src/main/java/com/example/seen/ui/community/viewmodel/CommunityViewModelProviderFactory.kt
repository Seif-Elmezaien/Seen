package com.example.seen.ui.community.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.seen.datasource.repository.AuthRepository
import com.example.seen.datasource.repository.CommunityRepository
import com.example.seen.datasource.repository.LogRepository
import com.example.seen.datasource.repository.UserRepository
import com.example.seen.ui.authentication.viewmodel.AuthViewModel
import com.example.seen.ui.home.viewmodel.HomeViewModel

class CommunityViewModelProviderFactory(
    val app: Application,
    val userRepository: UserRepository,
    val communityRepository: CommunityRepository
): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return CommunityViewModel(app, userRepository, communityRepository) as T
    }
}