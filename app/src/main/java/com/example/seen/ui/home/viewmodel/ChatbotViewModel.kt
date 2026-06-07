package com.example.seen.ui.home.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.seen.R
import com.example.seen.datasource.repository.ChatbotRepository
import com.example.seen.datasource.repository.LogRepository
import com.example.seen.datasource.repository.MedicineRepository
import com.example.seen.domain.model.chatbot.ChatMessage
import com.example.seen.domain.model.entites.Log
import com.example.seen.domain.model.entites.Medicine
import com.example.seen.domain.model.entites.RecordGlucose
import com.example.seen.domain.model.entites.RecordMeal
import com.example.seen.domain.model.entites.RecordMedication
import com.example.seen.util.SeenApplication
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChatbotViewModel(
    app: Application,
    private val chatbotRepository: ChatbotRepository
) : AndroidViewModel(app) {

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages = _messages.asStateFlow()

    private fun addMessage(message: ChatMessage) {
        _messages.value = _messages.value + message
    }

    private fun removeTyping() {
        _messages.value = _messages.value.filterNot { it.isTyping }
    }

    fun getHistory(token: String) {
        viewModelScope.launch {
            val response = chatbotRepository.getChatbotHistory(token)
            if(response.isSuccessful){
                response.body()?.history?.let { history ->
                    val mapped = history.map {
                        ChatMessage(
                            message = it.content,
                            isUser = it.role == "user"
                        )
                    }
                    _messages.value = mapped
                }
            }
        }
    }

    fun sendMessage(
        token: String,
        text: String
    ) {
        if(text.isBlank()) return

        viewModelScope.launch {
            addMessage(
                ChatMessage(
                    message = text,
                    isUser = true
                )
            )

            addMessage(
                ChatMessage(
                    message = "",
                    isUser = false,
                    isTyping = true
                )
            )
            try {
                val response =
                    chatbotRepository.askChatbot(
                        token,
                        text
                    )
                removeTyping()

                if (response.isSuccessful) {
                    val body = response.body()?.answer ?: "Something went wrong"
                    addMessage(ChatMessage(message = body, isUser = false))
                } else {
                    // Log the actual error
                    val errorBody = response.errorBody()?.string()
                    val code = response.code()

                    addMessage(ChatMessage(message = "Error $code: $errorBody", isUser = false))
                }
            } catch (e: Exception){
                removeTyping()
                addMessage(
                    ChatMessage(
                        message = getStringFromR(R.string.error_internet_connection),
                        isUser = false
                    )
                )
            }
        }
    }

    private fun getStringFromR(id: Int) =
        getApplication< SeenApplication>().getString(id)
}