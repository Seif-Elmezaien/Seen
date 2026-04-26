package com.example.seen.domain.model.entites

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    indices = [Index(value = ["email"], unique = true)]
)
data class User(
    @PrimaryKey(autoGenerate = false)
    var id: Int,
    var first_name: String,
    var last_name: String,
    var email: String,
    var password: String,
    var gender: String,
    var phone: String,
    var birthDate: String,
    var diabetes_type: String,
    var insulin_therapy: String,
    var glucose: String? = "mg/dl",
    var weight: Double,
    var height: Double,
    var max_glucose: Double? = null,
    var target_glucose_range: String? = null,
    var min_glucose: Double? = null,
    var emergency_contact: String? = null,
)