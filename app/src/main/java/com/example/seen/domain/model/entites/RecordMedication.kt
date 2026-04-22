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
data class RecordMedication(
    @PrimaryKey(autoGenerate = true)
    val medication_id: Int,
    val log_id: Int,
    val medication_name: String,
    val dosage: String,
    val unit: String,
    val frequency: String,
    val route: String,       // "oral", "injection", etc.
    val start_date: Long,
    val end_date: Long? = null,
    val reminder_time: Long? = null,
    val notes: String? = null,
)
