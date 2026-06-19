package com.example.seen.domain.model.community

import com.example.seen.domain.model.community.Data
import com.example.seen.domain.model.community.PostUser
import com.example.seen.domain.model.community.response.PostListResponse


data class SearchResult (
    val posts : PostListResponse,
    val users : List<PostUser>,
)