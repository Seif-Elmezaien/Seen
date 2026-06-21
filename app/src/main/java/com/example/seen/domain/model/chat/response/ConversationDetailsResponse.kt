package com.example.seen.domain.model.chat.response

import com.example.seen.domain.model.chat.ConversationUser
import com.example.seen.domain.model.chat.LatestMessage

data class ConversationDetailsResponse (
    val user1: ConversationUser,
    val user2: ConversationUser,
    val messages: List<LatestMessage>,
)