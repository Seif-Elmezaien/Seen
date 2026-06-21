package com.example.seen.domain.model.chat

data class LatestMessage(
    val id: Int,
    val conversation_id: Int,
    val sender_id: Int,
    val message: String?,
    val image_url: String?,
    val voice_url: String?,
    val video_url: String?,
    val is_read: Int,
    val created_at: String?,
    val updated_at: String?
)