package com.example.seen.datasource.repository

import com.example.seen.datasource.remote.RetrofitInstance
import com.example.seen.domain.model.User
import com.example.seen.domain.model.authentication.CheckEmailRequest
import com.example.seen.domain.model.authentication.CheckEmailResponse
import com.example.seen.domain.model.authentication.LoginAndSignupResponse
import com.example.seen.domain.model.authentication.LoginRequest
import com.example.seen.domain.model.authentication.SignupRequest
import retrofit2.Response

class AuthRepository {
    suspend fun login(loginRequest: LoginRequest): Response<LoginAndSignupResponse> =
        RetrofitInstance.api.login(loginRequest)

    suspend fun checkEmailExist(email: CheckEmailRequest): Response<CheckEmailResponse> =
        RetrofitInstance.api.checkEmailExist(email)

    suspend fun signup(user: SignupRequest): Response<LoginAndSignupResponse> =
        RetrofitInstance.api.signup(user)

}