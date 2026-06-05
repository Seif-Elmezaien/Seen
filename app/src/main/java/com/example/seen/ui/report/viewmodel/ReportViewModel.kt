package com.example.seen.ui.report.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.example.seen.datasource.repository.LogRepository
import com.example.seen.domain.model.entites.ReportStatistics
import com.example.seen.domain.model.entites.ReportFilter
import com.example.seen.util.Constants.Companion.WEEKLY
import kotlinx.coroutines.launch


class ReportViewModel(
    private val logRepository: LogRepository,
) : ViewModel() {


    var selectedPeriod = WEEKLY

    var fromDate: Long = 0L
    var toDate: Long = 0L

    private val filter = MutableLiveData<ReportFilter>()
    val statistics = MutableLiveData<ReportStatistics>()

    val graphData = filter.switchMap {
        logRepository.getGraphData(
            it.fromDate,
            it.toDate,
            it.readingType
        )
    }

    fun setFilter(reportFilter: ReportFilter) {
        fromDate= reportFilter.fromDate
        toDate = reportFilter.toDate
        selectedPeriod = reportFilter.period
        filter.value = reportFilter
        loadStatistics(reportFilter)
    }

    private fun loadStatistics(reportFilter: ReportFilter) {
        viewModelScope.launch {

            val count = logRepository.getGlucoseLogsCount(
                reportFilter.fromDate,
                reportFilter.toDate
            )

            val avg = logRepository.getAverageGlucose(
                reportFilter.fromDate,
                reportFilter.toDate
            )

            val lowest = logRepository.getLowestGlucoseLog(
                reportFilter.fromDate,
                reportFilter.toDate
            )

            val highest = logRepository.getHighestGlucoseLog(
                reportFilter.fromDate,
                reportFilter.toDate
            )

            statistics.value = ReportStatistics(
                logsCount = count,
                averageGlucose = avg,
                estimatedA1C = avg?.let { estimateA1C(it) },
                lowestLog = lowest,
                highestLog = highest
            )
        }
    }

    private fun estimateA1C(avgGlucoseMgDl: Float): Float {
        return (avgGlucoseMgDl + 46.7f) / 28.7f
    }
}