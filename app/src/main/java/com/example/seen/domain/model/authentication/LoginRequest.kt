package com.example.seen.domain.model.authentication

data class LoginRequest(
    val email: String,
    val password: String
)