package com.example.seen.domain.model.chat.response

import com.example.seen.domain.model.community.PostUser

data class ConversationSearchResponse(
    val results: List<PostUser>
)