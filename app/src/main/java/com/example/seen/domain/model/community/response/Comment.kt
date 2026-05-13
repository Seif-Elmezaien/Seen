package com.example.seen.domain.model.community.response

import com.example.seen.domain.model.community.PostUser


data class Comment(
    val comment_text : String,
    val created_at : String,
    val id: Int,
    val likes_count : Int,
    val post_id: Int,
    val updated_at: String,
    val user: PostUser,
    var isLiked: Boolean = false,
)