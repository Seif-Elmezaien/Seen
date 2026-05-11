package com.example.seen.datasource.repository

import com.example.seen.datasource.remote.RetrofitInstance
import com.example.seen.domain.model.community.response.PostListResponse
import retrofit2.Response

class CommunityRepository {

    suspend fun getCommunityPosts(page: Int, category: String): Response<PostListResponse> =
        RetrofitInstance.api.getCommunityPosts(page, category)

}