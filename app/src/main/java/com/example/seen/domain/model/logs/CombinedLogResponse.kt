package com.example.seen.domain.model.logs

data class CombinedLogResponse(
    val success: Boolean,
    val data: List<CombinedLogRequest>
)
