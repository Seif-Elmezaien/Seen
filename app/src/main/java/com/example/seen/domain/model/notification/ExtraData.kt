package com.example.seen.domain.model.notification

import com.example.seen.domain.model.community.Meta

data class ExtraData(
    val post_id: Int?,
    val username: String?,
    val likes_count: Int?
)
