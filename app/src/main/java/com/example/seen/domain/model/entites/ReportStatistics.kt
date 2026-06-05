package com.example.seen.domain.model.entites

data class ReportStatistics(
    val logsCount: Int,
    val averageGlucose: Float?,
    val estimatedA1C: Float?,
    val lowestLog: FullLog?,
    val highestLog: FullLog?
)
