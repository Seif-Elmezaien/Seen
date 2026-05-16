package com.example.seen.domain.model.community.request

import com.example.seen.domain.model.community.PostMedia

data class PostRequest (
    val title: String,
    val category: String,
    val content: String,
    val images: List<PostMedia>,
)