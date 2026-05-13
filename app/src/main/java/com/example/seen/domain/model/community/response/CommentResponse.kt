package com.example.seen.domain.model.community.response


data class CommentResponse(
    val post_id : Int? = null,
    val comments: MutableList<Comment>
)
