package com.example.seen.datasource.repository

import com.example.seen.datasource.remote.RetrofitInstance
import com.example.seen.domain.model.community.response.Comment
import com.example.seen.domain.model.community.response.CommentResponse
import com.example.seen.domain.model.community.response.PostListResponse
import com.example.seen.domain.model.community.response.SearchResponse
import retrofit2.Response

class CommunityRepository {

    suspend fun getCommunityPosts(token: String, page: Int, category: String): Response<PostListResponse> =
        RetrofitInstance.api.getCommunityPosts(token, page, category)

    suspend fun getPostComments(token: String, postId: Int, page: Int): Response<CommentResponse> =
        RetrofitInstance.api.getPostComments(token, postId, page)

    suspend fun searchPostAndUser(token: String, query: String, page: Int): Response<SearchResponse> =
        RetrofitInstance.api.searchPostAndUser(token, query, page)

}