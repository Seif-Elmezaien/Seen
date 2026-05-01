package com.example.seen.domain.model.entites

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity()
data class Medicines(
    @PrimaryKey(autoGenerate = true)
    val medicine_id : Int,
    val medicine_name : String,
)