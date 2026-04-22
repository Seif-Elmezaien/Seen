package com.example.seen.domain.model.entites

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    foreignKeys = [ForeignKey(
        entity = Logs::class,
        parentColumns = ["log_id"],
        childColumns = ["log_id"],
        onDelete = ForeignKey.CASCADE  // delete glucose if log is deleted
    )]
)
data class RecordGlucose(
    @PrimaryKey(autoGenerate = true)
    val reading_id: Int,
    val log_id: Int,
    val glucose_level: Float,
    val reading_time: Long,
    val reading_type: String, // "Fasting", "Before Meal", "After Meal", "Random".
    val notes: String? = null,
    val a1c_estimation: Float? = null,
)
