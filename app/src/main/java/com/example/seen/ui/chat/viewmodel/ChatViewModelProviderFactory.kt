package com.example.seen.ui.chat.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.seen.datasource.repository.ChatRepository
import com.example.seen.datasource.repository.ChatbotRepository

class ChatViewModelProviderFactory(
    val app: Application,
    val chatRepository: ChatRepository,
): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ChatViewModel(app, chatRepository) as T
    }
}