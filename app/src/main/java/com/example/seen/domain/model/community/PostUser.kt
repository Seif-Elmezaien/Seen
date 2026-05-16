package com.example.seen.domain.model.community

import java.io.Serializable
data class PostUser(
    val id: Int? =null,
    val first_name: String? ="",
    val last_name: String? ="",
    val full_name: String? ="",
    val diabetes_type: String? ="",
    val profile_picture: String,
) : Serializable