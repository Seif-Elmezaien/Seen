package com.example.seen.domain.model.chat

data class Conversation(
    val id: Int,
    val user1_id: Int,
    val user2_id: Int,
    val last_updated: String?,
    val created_at: String?,
    val updated_at: String?,
    val user1: ConversationUser?,
    val user2: ConversationUser?,
    val latest_message: LatestMessage?
)