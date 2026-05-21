package com.example.seen.domain.model.entites

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "deleted_logs")
data class DeletedLog(
    @PrimaryKey
    val log_id: String,
    val deleted_at: Long = System.currentTimeMillis()
)
