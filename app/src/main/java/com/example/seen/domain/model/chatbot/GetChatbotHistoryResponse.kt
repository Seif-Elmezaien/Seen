package com.example.seen.domain.model.chatbot

import com.example.seen.util.Resource

data class GetChatbotHistoryResponse (
    val success: Boolean,
    val history: List<ChatbotHistory>
)