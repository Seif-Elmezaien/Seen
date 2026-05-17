package com.example.seen.domain.model.entites

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(indices = [Index(value = ["medicine_name"], unique = true)])
data class Medicine(
    @PrimaryKey()
    val medicine_id : String = UUID.randomUUID().toString(),
    val medicine_name : String,
    val is_synced: Boolean = false
)