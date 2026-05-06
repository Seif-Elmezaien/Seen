package com.example.seen.domain.model.community

data class Data(
    val id: Int,
    val postUser: PostUser,
    val category: String,
    val title: String,
    val content: String,
    val post_media: List<PostMedia>,
    val comments_count: Int,
    val likes_count: Int,
    val created_at: String,
)