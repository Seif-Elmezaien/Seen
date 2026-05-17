package com.example.seen.domain.model.logs

data class CombinedLogRequestResponse(
    val log_id: String,
    val log_title: String?,
    val log_description: String?,
    val logged_at: String?, // backend expects "Y-m-d H:i:s" format
    val recordGlucose: GlucoseRequest?,
    val recordMeal: MealRequest?,
    val recordMedication: MedicationRequest?
)
