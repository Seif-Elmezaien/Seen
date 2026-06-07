package com.example.seen.domain.model.chatbot

data class AskChatbotResponse(
    val answer: String,
    val sources: List<ChatbotSource>? = null
)