package com.example.seen.domain.model.entites

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize
import java.util.UUID

@Parcelize
@Entity(tableName = "logs")
data class Log(
    @PrimaryKey()
    val log_id: String = UUID.randomUUID().toString(),
    val log_title: String,
    val log_description: String,
    val logged_at: Long = System.currentTimeMillis(), // store as timestamp
    val updated_at: Long = System.currentTimeMillis(),
    val is_synced: Boolean = false
) : Parcelable
