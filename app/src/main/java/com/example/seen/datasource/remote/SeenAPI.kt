package com.example.seen.datasource.remote

import com.example.seen.domain.model.authentication.CheckEmailRequest
import com.example.seen.domain.model.authentication.CheckEmailResponse
import com.example.seen.domain.model.authentication.LoginAndSignupResponse
import com.example.seen.domain.model.authentication.LoginRequest
import com.example.seen.domain.model.authentication.SignupRequest
import com.example.seen.domain.model.community.Comment
import com.example.seen.domain.model.community.PostUser
import com.example.seen.domain.model.community.request.CommentRequest
import com.example.seen.domain.model.community.request.PostRequest
import com.example.seen.domain.model.community.response.CommentResponse
import com.example.seen.domain.model.community.response.PostListResponse
import com.example.seen.domain.model.community.response.SearchResponse
import com.example.seen.domain.model.logs.CombinedLogRequestResponse
import retrofit2.Response
import retrofit2.http.*

interface SeenAPI {

    // ─── Authentication ───────────────────────────────────────────────────────

    @POST("login")
    suspend fun login(@Body request: LoginRequest): Response<LoginAndSignupResponse>

    @POST("check-email")
    suspend fun checkEmailExist(@Body request: CheckEmailRequest): Response<CheckEmailResponse>

    @POST("register")
    suspend fun signup(@Body request: SignupRequest): Response<LoginAndSignupResponse>

    // ─── Logs ───────────────────────────────────────────────────────

    @POST("logs/android")
    suspend fun uploadLog(
        @Header("Authorization") token: String,
        @Body log: CombinedLogRequestResponse)
    : Response<Unit>

    @PUT("logs")
    suspend fun updateLog(
        @Body log: CombinedLogRequestResponse
    ): Response<Unit>

    @DELETE("logs")
    suspend fun deleteLog( @Body log: CombinedLogRequestResponse ): Response<Unit>

    @GET("logs")
    suspend fun getLogs(): Response<List<CombinedLogRequestResponse>>

    // ─── Posts ────────────────────────────────────────────────────────────────

    @GET("posts")
    suspend fun getCommunityPosts(
        @Header("Authorization") token: String,
        @Query("page") page: Int = 1,
        @Query("category") category: String,
    ): Response<PostListResponse>

    @POST("posts")
    suspend fun createPost(
        @Header("Authorization") token: String,
        @Body post: PostRequest,
    ): Response<PostListResponse>

    @PUT("posts/{postId}")
    suspend fun editPost(
        @Header("Authorization") token: String,
        @Path("postId") postId: Int,
        @Body post: PostRequest,
    ): Response<PostListResponse>

    @DELETE("posts/{postId}")
    suspend fun deletePost(
        @Header("Authorization") token: String,
        @Path("postId") postId: Int,
    ): Response<Unit>

    @POST("posts/{postId}/like")
    suspend fun likePost(
        @Header("Authorization") token: String,
        @Path("postId") postId: Int,
    ): Response<Unit>

    @GET("posts/{post_id}/likes")
    suspend fun getPostLikes(
        @Header("Authorization") token: String,
        @Path("postId") postId: Int,
    ): PostUser

    // ─── Comments ─────────────────────────────────────────────────────────────

    @GET("posts/{postId}/comments")
    suspend fun getPostComments(
        @Header("Authorization") token: String,
        @Path("postId") postId: Int,
        @Query("page") page: Int = 1,
    ): Response<CommentResponse>

    @POST("posts/{postId}/comments")
    suspend fun addComment(
        @Header("Authorization") token: String,
        @Path("postId") postId: Int,
        @Body comment: CommentRequest,
    ): Response<Comment>

    @PUT("comments/{commentId}")
    suspend fun editComment(
        @Header("Authorization") token: String,
        @Path("commentId") commentId: Int,
        @Body comment: CommentRequest,
    ): Response<Comment>

    @DELETE("comments/{commentId}")
    suspend fun deleteComment(
        @Header("Authorization") token: String,
        @Path("commentId") commentId: Int,
    ): Response<Unit>

    @POST("comments/{commentId}/like")
    suspend fun likeComment(
        @Header("Authorization") token: String,
        @Path("commentId") commentId: Int,
    ): Response<Unit>

    @GET("comments/{comment_id}/likes")
    suspend fun getCommentLikes(
        @Header("Authorization") token: String,
        @Path("comment_id") commentId: Int,
    ): PostUser

    // ─── Search ───────────────────────────────────────────────────────────────

    @GET("search")
    suspend fun searchPostAndUser(
        @Header("Authorization") token: String,
        @Query("q") query: String,
        @Query("page") page: Int = 1,
    ): Response<SearchResponse>

}