package com.example.seen.util

import android.content.Context
import android.net.ConnectivityManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Converts a Long timestamp (milliseconds) to a formatted date string
// that the backend expects: "2026-05-21 04:30:00"
fun Long.toFormattedDate(): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
    return sdf.format(Date(this))
}

fun Long.toReportFormattedDate(): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
    return sdf.format(Date(this))
}

// Converts a date string from the server back to a Long timestamp (milliseconds)
// handles two formats:
// 1. ISO 8601 from Laravel: "2026-05-21T04:30:00.000000Z"
// 2. Normal format: "2026-05-21 04:30:00"
// returns current time if null or parsing fails
fun String?.toTimestamp(): Long {
    if (this == null) return System.currentTimeMillis()
    return try {
        // first try Laravel's ISO 8601 format
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.ENGLISH)
        sdf.parse(this)?.time ?: System.currentTimeMillis()
    } catch (e: Exception) {
        try {
            // fallback to normal format if ISO 8601 fails
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
            sdf.parse(this)?.time ?: System.currentTimeMillis()
        } catch (e: Exception) {
            // if both fail return current time
            System.currentTimeMillis()
        }
    }
}

// Checks if the device currently has an active internet connection
// returns true if connected, false if offline
fun Context.isOnline(): Boolean {
    val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    return cm.activeNetwork != null
}