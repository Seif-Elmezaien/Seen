package com.example.seen.domain.model.entites

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity()
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
    var glucose: String?,
    var weight: Double,
    var height: Double,
    var max_glucose: Double?,
    var target_glucose_range: String?,
    var min_glucose: Double?,
    var emergency_contact: String?,
)