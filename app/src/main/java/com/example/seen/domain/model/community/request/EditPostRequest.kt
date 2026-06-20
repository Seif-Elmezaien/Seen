package com.example.seen.domain.model.community.request

data class EditPostRequest(
    val title: String,
    val content: String,
    val category: String,
)