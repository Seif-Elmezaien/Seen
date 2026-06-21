package com.example.seen.ui.chat.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.seen.R
import com.example.seen.datasource.repository.ChatbotRepository
import com.example.seen.domain.model.chatbot.ChatMessage
import com.example.seen.util.SeenApplication
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChatViewModel(
    app: Application,
    private val chatbotRepository: ChatbotRepository
) : AndroidViewModel(app) {


}