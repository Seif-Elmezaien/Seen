package com.example.seen.datasource.repository

import com.example.seen.datasource.remote.RetrofitInstance
import com.example.seen.domain.model.community.Comment
import com.example.seen.domain.model.community.request.CommentRequest
import com.example.seen.domain.model.community.request.PostRequest
import com.example.seen.domain.model.community.response.CommentResponse
import com.example.seen.domain.model.community.response.PostListResponse
import com.example.seen.domain.model.community.response.SearchResponse
import retrofit2.Response

class CommunityRepository {

// ─── Posts ─────────────────────────────────────────────────────────────

    suspend fun getCommunityPosts(token: String, page: Int, category: String): Response<PostListResponse> =
        RetrofitInstance.api.getCommunityPosts(token, page, category)

    suspend fun createPost(token: String, post: PostRequest): Response<PostListResponse> =
        RetrofitInstance.api.createPost(token, post)

    suspend fun editPost(token: String, postId: Int, post: PostRequest): Response<PostListResponse> =
        RetrofitInstance.api.editPost(token, postId, post)

    suspend fun deletePost(token: String, postId: Int): Response<Unit> =
        RetrofitInstance.api.deletePost(token, postId)

    suspend fun likePost(token: String, postId: Int): Response<Unit> =
        RetrofitInstance.api.likePost(token, postId)

    suspend fun getPostLikes(token: String, postId: Int) =
        RetrofitInstance.api.getPostLikes(token, postId)

// ─── Comments ─────────────────────────────────────────────────────────────

    suspend fun getPostComments(token: String, postId: Int, page: Int): Response<CommentResponse> =
        RetrofitInstance.api.getPostComments(token, postId, page)

    suspend fun addComment(token: String, postId: Int, comment: CommentRequest): Response<Comment> =
        RetrofitInstance.api.addComment(token, postId, comment)

    suspend fun editComment(token: String, commentId: Int, comment: CommentRequest): Response<Comment> =
        RetrofitInstance.api.editComment(token, commentId, comment)

    suspend fun deleteComment(token: String, commentId: Int): Response<Unit> =
        RetrofitInstance.api.deleteComment(token, commentId)

    suspend fun likeComment(token: String, commentId: Int): Response<Unit> =
        RetrofitInstance.api.likeComment(token, commentId)

    suspend fun getCommentLikes(token: String, commentId: Int) =
        RetrofitInstance.api.getCommentLikes(token, commentId)

// ─── Search ───────────────────────────────────────────────────────────────

    suspend fun searchPostAndUser(token: String, query: String, page: Int): Response<SearchResponse> =
        RetrofitInstance.api.searchPostAndUser(token, query, page)
}