package com.example.seen.domain.model.entites

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    foreignKeys = [ForeignKey(
        entity = Log::class,
        parentColumns = ["log_id"],
        childColumns = ["log_id"],
        onDelete = ForeignKey.CASCADE  // delete glucose if log is deleted
    )],
    indices = [Index(value = ["log_id"], unique = true)]
)
data class RecordMeal(
    @PrimaryKey(autoGenerate = true)
    val meal_id: Int,
    val log_id: Int,
    val meal_time: Long,
    val total_carb: Float? = null,
    val total_calories: Float? = null,
    val meal_type: String, // "Breakfast", "Lunch", "Dinner", "Random"
    val notes: String? = null,
)
