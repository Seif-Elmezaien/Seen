package com.example.seen.ui.authentication.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.seen.datasource.repository.AuthRepository
import com.example.seen.datasource.repository.UserRepository

class AuthViewModelProviderFactory(
    val app: Application,
    val authRepository: AuthRepository,
    val userRepository: UserRepository
): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AuthViewModel(app, authRepository, userRepository) as T
    }
}