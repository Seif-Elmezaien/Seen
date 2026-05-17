package com.example.seen.domain.model.logs

data class MealRequest(
    val meal_type: String,
    val meal_description: String,
    val total_carb: Int?,
    val total_calories: Int?,
    val notes: String?
)
