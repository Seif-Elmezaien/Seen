package com.example.seen.domain.model.community.response

import com.example.seen.domain.model.community.Comment


data class CommentResponse(
    val post_id : Int? = null,
    val comments: MutableList<Comment>
)
