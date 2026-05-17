package com.example.seen.domain.model.logs

data class GlucoseRequest(
    val reading_type: String,
    val glucose_level: Int,
    val a1c_estimation: Float?,
    val notes: String?
)
