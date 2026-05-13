package com.example.seen.domain.model.authentication

import com.example.seen.domain.model.entites.User

data class LoginAndSignupResponse(
    val message: String? = null,
    val user: User? = null,
    val token: String? = null
)
