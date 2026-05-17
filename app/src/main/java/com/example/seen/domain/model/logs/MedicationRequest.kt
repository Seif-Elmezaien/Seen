package com.example.seen.domain.model.logs

import com.example.seen.domain.model.entites.SelectedMedication

data class MedicationRequest(
    val medications: List<SelectedMedication>,
    val notes: String?
)
