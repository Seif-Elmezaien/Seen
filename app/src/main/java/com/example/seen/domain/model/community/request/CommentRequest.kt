package com.example.seen.domain.model.community.request

import com.google.gson.annotations.SerializedName

data class CommentRequest(
    @SerializedName("comment_text") val commentText: String
)