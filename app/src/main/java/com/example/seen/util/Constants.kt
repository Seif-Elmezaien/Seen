package com.example.seen.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class Constants {
    companion object{
        const val BASE_URL2 = "https://ollie-wroth-tributarily.ngrok-free.dev/api/" // Loay
        const val BASE_URL = "https://inquisitorial-elba-undistractedly.ngrok-free.dev/api/" // Ziad
        const val SEARCH_POST_TIME_DELAY = 500L

        const val LOW_GLUCOSE_VALUE = 70
        const val HIGH_GLUCOSE_VALUE = 180

        const val POST_PAGE_SIZE = 10
        const val COMMENT_PAGE_SIZE = 20

        const val NAV_ANIM_DURATION = 220L
    }
}

fun Long.toFormattedDate(): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
    return sdf.format(Date(this))
}

fun String?.toTimestamp(): Long {
    if (this == null) return System.currentTimeMillis()
    return try {
        // try ISO 8601 format first (from server)
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.getDefault())
        sdf.parse(this)?.time ?: System.currentTimeMillis()
    } catch (e: Exception) {
        try {
            // fallback to normal format
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            sdf.parse(this)?.time ?: System.currentTimeMillis()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }
}