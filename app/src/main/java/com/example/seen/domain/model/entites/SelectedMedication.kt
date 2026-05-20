package com.example.seen.domain.model.entites

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class SelectedMedication(
    val medication_name: String
) : Parcelable
