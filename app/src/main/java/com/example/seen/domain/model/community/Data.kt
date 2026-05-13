package com.example.seen.domain.model.community

import java.io.Serializable

data class Data(
    val id: Int,
    val user: PostUser,
    val category: String,
    val title: String,
    val content: String,
    val images: List<PostMedia>,
    val comments_count: Int,
    val likes_count: Int,
    val created_at: String,
    val updated_at: String,
    val is_liked: Boolean? = false,
    ) : Serializable