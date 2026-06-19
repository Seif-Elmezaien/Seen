package com.example.seen.datasource.remote

import com.example.seen.domain.model.authentication.CheckEmailRequest
import com.example.seen.domain.model.authentication.CheckEmailResponse
import com.example.seen.domain.model.authentication.LoginAndSignupResponse
import com.example.seen.domain.model.authentication.LoginRequest
import com.example.seen.domain.model.authentication.SignupRequest
import com.example.seen.domain.model.chatbot.AskChatbotRequest
import com.example.seen.domain.model.chatbot.AskChatbotResponse
import com.example.seen.domain.model.chatbot.GetChatbotHistoryResponse
import com.example.seen.domain.model.community.Data
import com.example.seen.domain.model.community.PostUser
import com.example.seen.domain.model.community.request.CommentRequest
import com.example.seen.domain.model.community.request.PostRequest
import com.example.seen.domain.model.community.response.AddCommentResponse
import com.example.seen.domain.model.community.response.CommentResponse
import com.example.seen.domain.model.community.response.PostCommentLikesResponse
import com.example.seen.domain.model.community.response.PostListResponse
import com.example.seen.domain.model.community.response.SearchResponse
import com.example.seen.domain.model.logs.LogResponse
import com.example.seen.domain.model.logs.LogRequest
import com.example.seen.domain.model.notification.FcmTokenRequest
import com.example.seen.domain.model.notification.MarkReadResponse
import com.example.seen.domain.model.notification.NotificationsResponse
import com.example.seen.domain.model.profile.response.ProfileResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
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

    // ─── Notification ───────────────────────────────────────────────────────

    @POST("user/save-device-token")
    suspend fun updateFcmToken(
        @Header("Authorization") token: String,
        @Body device_token: FcmTokenRequest
    ): Response<Unit>

    @GET("notifications")
    suspend fun getNotifications(
        @Header("Authorization") token: String
    ): Response<NotificationsResponse>

    @POST("notifications/{id}/read")
    suspend fun markAsRead(
        @Header("Authorization") token: String,
        @Path("id") notificationId: Int
    ): Response<MarkReadResponse>

    @POST("notifications/read-all")
    suspend fun markAllAsRead(
        @Header("Authorization") token: String
    ): Response<MarkReadResponse>

    // ─── Logs ───────────────────────────────────────────────────────

    @POST("logs/android")
    suspend fun uploadLog(
        @Header("Authorization") token: String,
        @Body log: LogRequest)
    : Response<Unit>

    @PUT("logs")
    suspend fun updateLog(
        @Header("Authorization") token: String,
        @Body log: LogRequest
    ): Response<Unit>

    @DELETE("logs/{logId}")
    suspend fun deleteLog(
        @Header("Authorization") token: String,
        @Path("logId") logId: String
    ): Response<Unit>

    @GET("logs/sync")
    suspend fun syncLogs(
        @Header("Authorization") token: String,
        @Query("last_sync") updatedSince: String?= null
    ): Response<LogResponse>

    // ─── Reminders ────────────────────────────────────────────────────────────────

    // ─── Posts ────────────────────────────────────────────────────────────────

    @GET("posts")
    suspend fun getCommunityPosts(
        @Header("Authorization") token: String,
        @Query("page") page: Int = 1,
        @Query("category") category: String,
    ): Response<PostListResponse>

    @GET("my-posts")
    suspend fun getMyPosts(
        @Header("Authorization") token: String,
        @Query("page") page: Int = 1,
    ): Response<PostListResponse>

    @GET("users/{user}/posts")
    suspend fun getUserPosts(
        @Header("Authorization") token: String,
        @Path("user") userId: Int,
        @Query("page") page: Int = 1,
    ): Response<PostListResponse>

    @Multipart
    @POST("posts")
    suspend fun createPost(
        @Header("Authorization") token: String,
        @Part("title")    title: RequestBody,
        @Part("content")  content: RequestBody,
        @Part("category") category: RequestBody,
        @Part images: List<MultipartBody.Part>
    ): Response<Data>

    @PUT("posts/{postId}")
    suspend fun editPost(
        @Header("Authorization") token: String,
        @Path("postId") postId: Int,
        @Body post: PostRequest,
    ): Response<Data>

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

    @GET("posts/{postId}/likes")
    suspend fun getPostLikes(
        @Header("Authorization") token: String,
        @Path("postId") postId: Int,
    ): Response<PostCommentLikesResponse>

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
    ): Response<AddCommentResponse>

    @PUT("comments/{commentId}")
    suspend fun editComment(
        @Header("Authorization") token: String,
        @Path("commentId") commentId: Int,
        @Body comment: CommentRequest,
    ): Response<AddCommentResponse>

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
    ): Response<PostCommentLikesResponse>

    // ─── Search ───────────────────────────────────────────────────────────────

    @GET("search")
    suspend fun searchPostAndUser(
        @Header("Authorization") token: String,
        @Query("query") query: String,
        @Query("page") page: Int = 1,
    ): Response<SearchResponse>

    // ─── Profile ────────────────────────────────────────────────────────────────

    @GET("user/profile")
    suspend fun getProfile(
        @Header("Authorization") token: String,
    ) : Response<ProfileResponse>

    // ─── Friendship ────────────────────────────────────────────────────────────────

    @POST("friends/{id}/request")
    suspend fun sendFriendRequest(
        @Header("Authorization") token: String,
        @Path("id") userId: Int,
    )

    @POST("friends/{id}/accept")
    suspend fun acceptFriendRequest(
        @Header("Authorization") token: String,
        @Path("id") userId: Int,
    )

    @DELETE("friends/{id}/cancel")
    suspend fun cancelFriendRequest(
        @Header("Authorization") token: String,
        @Path("id") userId: Int,
    )

    @DELETE("friends/{id}")
    suspend fun removeFriend(
        @Header("Authorization") token: String,
        @Path("id") userId: Int,
    )

    @POST("friends/{id}/block")
    suspend fun blockFriend(
        @Header("Authorization") token: String,
        @Path("id") userId: Int,
    )

    @DELETE("friends/{id}/unblock")
    suspend fun unblockFriend(
        @Header("Authorization") token: String,
        @Path("id") userId: Int,
    )

    @GET("friends/blocks")
    suspend fun getBlockedUsers(
        @Header("Authorization") token: String,
    ): Response<List<PostUser>>

    @POST("friends/profile")
    suspend fun getFriendsProfile(
        @Header("Authorization") token: String,
    ): Response<List<PostUser>>

    // ─── Report ───────────────────────────────────────────────────────────────
    @GET("reports/glucose/pdf")
    suspend fun generateReport(
        @Header("Authorization") token: String,

        @Query("start_date")     startDate: String,
        @Query("end_date")       endDate: String
    ): Response<ResponseBody>

    // ─── Chatbot ───────────────────────────────────────────────────────────────

    @POST("chatbot/ask")
    suspend fun askChatbot(
        @Header("Authorization") token: String,
        @Body message: AskChatbotRequest
    ): Response<AskChatbotResponse>

    @GET("chatbot/history")
    suspend fun getChatbotHistory(
        @Header("Authorization") token: String
    ) : Response<GetChatbotHistoryResponse>

}