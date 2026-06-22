package com.example.seen.domain.model.chat

data class SendMessageResponse(
    val message: String,
    val data: ChatMessage
)