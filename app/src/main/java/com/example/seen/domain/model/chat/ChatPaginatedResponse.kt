package com.example.seen.domain.model.chat

data class ChatPaginatedResponse<T>(
    val current_page: Int,
    val data: List<T>,
    val next_page_url: String?,
    val last_page: Int,
    val total: Int
)
