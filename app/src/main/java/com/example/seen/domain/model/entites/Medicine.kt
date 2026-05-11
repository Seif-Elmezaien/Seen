package com.example.seen.domain.model.entites

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(indices = [Index(value = ["medicine_name"], unique = true)])
data class Medicine(
    @PrimaryKey(autoGenerate = true)
    val medicine_id : Int,
    val medicine_name : String,
)