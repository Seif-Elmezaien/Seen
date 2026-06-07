package com.example.seen.domain.model.chatbot

data class ChatMessage(
    val message: String,
    val isUser: Boolean,
    val isTyping: Boolean = false
)
