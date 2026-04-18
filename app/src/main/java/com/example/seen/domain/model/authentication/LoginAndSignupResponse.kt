package com.example.seen.domain.model.authentication

import com.example.seen.domain.model.User

data class LoginAndSignupResponse(
    val message: String,
    val user: User,
    val token: String
)
