package com.example.seen.domain.model.community.response

import com.example.seen.domain.model.community.PostUser


data class Comment(
    val comment_text : String? = "",
    val created_at : String? = "",
    val id: Int? = null,
    val likes_count : Int? = null,
    val post_id: Int? = null,
    val updated_at: String? ="",
    val user: PostUser,
    var isLiked: Boolean = false,
)