package com.example.seen.domain.model.entites

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity()
data class Reminder(
    @PrimaryKey(autoGenerate = true)
    val reminder_id : Int,
    val message_type : String, // glucose, medication, meal
    val message : String,
    val time: Long,
    val medication_name: String? = null,
    val status: String // Done, Still, Skipped
)