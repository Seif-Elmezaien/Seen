package com.example.seen.domain.model.chatbot

data class ChatbotHistory(
    val role: String,
    val content: String,
    val created_at: String
)
