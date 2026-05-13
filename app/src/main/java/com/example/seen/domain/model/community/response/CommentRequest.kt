package com.example.seen.domain.model.community.response

data class CommentRequest(
    val postId: Int,
    val content: String,
    val token: String,
)