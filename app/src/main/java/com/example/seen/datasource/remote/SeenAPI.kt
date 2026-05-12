package com.example.seen.datasource.remote

import com.example.seen.domain.model.authentication.CheckEmailRequest
import com.example.seen.domain.model.authentication.CheckEmailResponse
import com.example.seen.domain.model.authentication.LoginAndSignupResponse
import com.example.seen.domain.model.authentication.LoginRequest
import com.example.seen.domain.model.authentication.SignupRequest
import com.example.seen.domain.model.community.response.PostListResponse
import com.example.seen.domain.model.community.PostResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface SeenAPI {

    /**
     * Authentication
     */
    // Login Check
    @POST("login")
    suspend fun login(
        @Body loginRequest: LoginRequest,
    ) : Response<LoginAndSignupResponse>

    // Check if email exist
    @POST("check-email")
    suspend fun checkEmailExist(
        @Body email: CheckEmailRequest
    ) : Response<CheckEmailResponse>

    // Signing User in
    @POST("register")
    suspend fun signup(
        @Body user: SignupRequest
    ) : Response<LoginAndSignupResponse>


    /**
     * Community
     */
    // Get Posts
    @GET("posts")
    suspend fun getCommunityPosts(
        @Header("Authorization")
        token: String,
        @Query("page")
        page: Int = 1,
        @Query("category")
        category: String,
    ) : Response<PostListResponse>


}