package com.example.seen.domain.model.profile

data class ProfileData(
    val id: Int,
    val full_name: String,
    val profile_picture: String,
    val diabetes_type: String,
    val friends_count: Int,
    val relation_status: String, // (me, blocked, friends, pending_sent, pending_received, add_friend)
    val posts_count: Int
)
