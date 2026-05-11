package com.example.seen.domain.model.community

data class PostUser(
    val id: Int,
    val first_name: String,
    val last_name: String,
    val full_name: String,
    val diabetes_type: String,
    val profile_picture: String,
    val created_at: String
)