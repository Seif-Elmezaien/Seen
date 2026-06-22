package com.example.seen.domain.model.chat

data class ChatUser(
    val id: Int,
    val first_name: String,
    val last_name: String,
    val profile_picture: String?
)