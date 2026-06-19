package com.example.seen.domain.model.community.response

import com.example.seen.domain.model.community.Data
import com.example.seen.domain.model.community.Meta

data class PostListResponse(
    val data: MutableList<Data>,
    val meta: Meta
)