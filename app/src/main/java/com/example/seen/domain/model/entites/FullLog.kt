package com.example.seen.domain.model.entites

import androidx.room.Embedded
import androidx.room.Relation

data class FullLog(
    @Embedded
    val logs: Logs,

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
)
