package com.example.seen.util

import android.content.Context
import android.net.ConnectivityManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class Constants {
    companion object{
        const val BASE_URL2 = "https://ollie-wroth-tributarily.ngrok-free.dev/api/" // Loay
        const val BASE_URL = "https://inquisitorial-elba-undistractedly.ngrok-free.dev/api/" // Ziad
        const val SEARCH_POST_TIME_DELAY = 500L

        const val LOW_GLUCOSE_VALUE = 70
        const val HIGH_GLUCOSE_VALUE = 220

        // Diabetes types
        const val TYPE_1 = "Type1"
        const val TYPE_2 = "Type2"
        const val LADA = "LADA"
        const val MODY = "MODY"
        const val GESTATIONAL = "Gestational"
        const val OTHER = "other"
        const val PEN_SYRINGES = "Pen / Syringes"
        const val PUMP = "pump"
        const val NO_INSULIN = "No insulin"

        // Community categories
        const val GENERAL = "General"
        const val TYPE1_LADA = "Type1 / LADA"
        const val ADVICES = "Advices"

        const val POST_PAGE_SIZE = 10
        const val COMMENT_PAGE_SIZE = 20

        const val NAV_ANIM_DURATION = 220L
    }
}