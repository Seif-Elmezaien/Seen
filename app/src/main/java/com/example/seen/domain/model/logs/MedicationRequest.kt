package com.example.seen.domain.model.logs


data class MedicationRequest(
    val medications: List<String>,
    val notes: String?
)
