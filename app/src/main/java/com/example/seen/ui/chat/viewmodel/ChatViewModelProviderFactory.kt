package com.example.seen.ui.chat.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.seen.datasource.repository.ChatRepository
import com.example.seen.datasource.repository.ChatbotRepository
import com.example.seen.datasource.repository.UserRepository

class ChatViewModelProviderFactory(
    private val app: Application,
    private val chatRepository: ChatRepository,
    private val userRepository: UserRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ChatViewModel(app, chatRepository, userRepository) as T
    }
}