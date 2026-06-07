package com.example.seen.ui.chatbot.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.seen.datasource.repository.ChatbotRepository

class ChatbotViewModelProviderFactory(
    val app: Application,
    val chatbotRepository: ChatbotRepository,
): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ChatbotViewModel(app, chatbotRepository) as T
    }
}