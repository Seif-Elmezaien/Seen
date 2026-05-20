package com.example.seen.domain.model.entites

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize
import java.util.UUID

@Parcelize
@Entity(
    foreignKeys = [ForeignKey(
        entity = Log::class,
        parentColumns = ["log_id"],
        childColumns = ["log_id"],
        onDelete = ForeignKey.CASCADE  // delete glucose if log is deleted
    )],
    indices = [Index(value = ["log_id"], unique = true)]
)
data class RecordMedication(
    @PrimaryKey(autoGenerate = true)
    val medication_id: Int,
    val log_id: String,
    val medications: List<SelectedMedication>,
    val notes: String? = null,
): Parcelable
