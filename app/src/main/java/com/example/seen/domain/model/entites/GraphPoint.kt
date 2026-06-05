package com.example.seen.domain.model.entites

data class GraphPoint(
    val glucoseValue: Int,
    val readingType: String,
    val loggedAt: Long,
    val logId: String
)
