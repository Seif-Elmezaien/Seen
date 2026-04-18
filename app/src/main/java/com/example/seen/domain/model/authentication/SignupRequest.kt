package com.example.seen.domain.model.authentication

import java.util.Date


data class SignupRequest (
    var first_name: String? = null,
    var last_name: String? = null,
    var email: String? = null,
    var password: String? = null,
    var phone: String? = null,
    var gender: String? = null,
    var birthDate: String? = null,
    var diabetes_type: String? = null,
    var insulin_therapy: String? = null,
    var weight: Double? = null,
    var height: Double? = null,
)