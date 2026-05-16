package com.example.seen.datasource.remote

import com.example.seen.domain.model.authentication.CheckEmailRequest
import com.example.seen.domain.model.authentication.CheckEmailResponse
import com.example.seen.domain.model.authentication.LoginAndSignupResponse
import com.example.seen.domain.model.authentication.LoginRequest
import com.example.seen.domain.model.authentication.SignupRequest
import com.example.seen.domain.model.community.response.PostListResponse
import com.example.seen.domain.model.community.response.Comment
import com.example.seen.domain.model.community.response.CommentResponse
import com.example.seen.domain.model.community.response.SearchResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
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

    //get post comment
    @GET("posts/{postId}/comments")
    suspend fun getPostComments(
        @Header("Authorization")
        token: String,
        @Path("postId") postId: Int,
        @Query("page")
        page: Int = 1,
    ) : Response<CommentResponse>

    @GET("/search")
    suspend fun searchPostAndUser(
        @Header("Authorization")
        token: String,
        @Query("q")
        query: String,
        @Query("page")
        page: Int = 1,
    ) : Response<SearchResponse>


}