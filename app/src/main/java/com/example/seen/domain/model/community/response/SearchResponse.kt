package com.example.seen.domain.model.community.response

import com.example.seen.domain.model.community.PostUser


data class SearchResponse (
    val posts : PostListResponse,
    val users : List<PostUser>,
)