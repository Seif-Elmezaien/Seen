package com.example.seen.datasource.remote

import com.example.seen.domain.model.authentication.CheckEmailRequest
import com.example.seen.domain.model.authentication.CheckEmailResponse
import com.example.seen.domain.model.authentication.LoginAndSignupResponse
import com.example.seen.domain.model.authentication.LoginRequest
import com.example.seen.domain.model.authentication.SignupRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface SeenAPI {

    // Login Check
    @POST("api/login")
    suspend fun login(
        @Body loginRequest: LoginRequest,
    ) : Response<LoginAndSignupResponse>

    // Check if email exist
    @POST("api/check-email")
    suspend fun checkEmailExist(
        @Body email: CheckEmailRequest
    ) : Response<CheckEmailResponse>

    // Signing User in
    @POST("api/register")
    suspend fun signup(
        @Body user: SignupRequest
    ) : Response<LoginAndSignupResponse>

}