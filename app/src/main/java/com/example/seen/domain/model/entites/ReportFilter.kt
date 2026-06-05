package com.example.seen.domain.model.entites

import com.example.seen.util.Constants.Companion.WEEKLY

data class ReportFilter(
    val fromDate: Long,
    val toDate: Long,
    val period: String,
    val readingType: String? = null
)