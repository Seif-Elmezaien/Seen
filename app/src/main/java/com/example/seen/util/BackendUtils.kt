package com.example.seen.util

import android.content.Context
import android.net.ConnectivityManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

private val cairoTZ = TimeZone.getTimeZone("Africa/Cairo")

// Converts a Long timestamp (milliseconds) to a formatted date string
// that the backend expects: "2026-05-21 04:30:00"
fun Long.toFormattedDate(): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
    sdf.timeZone = cairoTZ
    return sdf.format(Date(this))
}

fun Long.toReportFormattedDate(): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
    sdf.timeZone = cairoTZ
    return sdf.format(Date(this))
}

// Converts a date string from the server back to a Long timestamp (milliseconds)
// handles two formats:
// 1. Community posts format: "2026-06-07 01:12:35 PM"
// 2. Normal format: "2026-05-21 04:30:00"
// returns current time if null or parsing fails
fun String?.toTimestamp(): Long {
    if (this == null) return System.currentTimeMillis()

    val formats = listOf(
        SimpleDateFormat("yyyy-MM-dd hh:mm:ss a", Locale.ENGLISH),  // "2026-06-07 01:12:35 PM"
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH),    // "2026-05-21 04:30:00"
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.ENGLISH) // ISO fallback
    )

    for (sdf in formats) {
        sdf.timeZone = cairoTZ
        try {
            val result = sdf.parse(this)
            if (result != null) return result.time
        } catch (e: Exception) {
            continue
        }
    }

    return System.currentTimeMillis()
}

// Checks if the device currently has an active internet connection
// returns true if connected, false if offline
fun Context.isOnline(): Boolean {
    val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    return cm.activeNetwork != null
}