package com.example.seen.domain.model.entites

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "logs")
data class Log(
    @PrimaryKey(autoGenerate = true)
    val log_id : Int,
    val log_title: String,
    val log_description: String,
    val logged_at: Long = System.currentTimeMillis(), // store as timestamp
    val updated_at: Long = System.currentTimeMillis()
)
