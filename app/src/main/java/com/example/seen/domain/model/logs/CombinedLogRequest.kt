package com.example.seen.domain.model.logs

data class CombinedLogRequest(
    val log_id: String,
    val log_title: String?,
    val log_description: String?,
    val logged_at: String?, // backend expects "Y-m-d H:i:s" format
    val record_glucose: GlucoseRequest?,
    val record_meal: MealRequest?,
    val record_medication: MedicationRequest?
)
