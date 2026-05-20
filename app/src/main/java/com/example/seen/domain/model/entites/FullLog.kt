package com.example.seen.domain.model.entites

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Relation
import kotlinx.android.parcel.Parcelize

@Parcelize
data class FullLog(
    @Embedded
    val log: Log,

    @Relation(
    parentColumn = "log_id",   // the log_id in Logs
    entityColumn = "log_id"    // the log_id in RecordGlucose
    )
    val glucose: RecordGlucose?,

    @Relation(
    parentColumn = "log_id",
    entityColumn = "log_id"
    )
    val meal: RecordMeal?,

    @Relation(
    parentColumn = "log_id",
    entityColumn = "log_id"
    )
    val medication: RecordMedication?
) : Parcelable
