package com.example.seen.domain.model.community.response

import com.example.seen.domain.model.community.PostUser

data class PostCommentLikesResponse(
    val post_id: Int,
    val total_likes: Int,
    val users: List<PostUser>
)