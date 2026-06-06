package com.example.seen.ui.report.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.example.seen.R
import com.example.seen.datasource.repository.LogRepository
import com.example.seen.domain.model.entites.ReportStatistics
import com.example.seen.domain.model.entites.ReportFilter
import com.example.seen.util.Constants.Companion.WEEKLY
import com.example.seen.util.NetworkUtils
import com.example.seen.util.Resource
import com.example.seen.util.SeenApplication
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import okio.IOException
import retrofit2.Response


class ReportViewModel(
    app: Application,
    private val logRepository: LogRepository,
) : AndroidViewModel(app) {

    var selectedPeriod = WEEKLY

    var fromDate: Long = 0L
    var toDate: Long = 0L

    private val _downloadReportState =
        MutableLiveData<Resource<ResponseBody>?>()

    val downloadReportState: LiveData<Resource<ResponseBody>?> =
        _downloadReportState


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

    fun generateReport(token: String) = viewModelScope.launch {

        _downloadReportState.postValue(Resource.Loading())

        _downloadReportState.postValue(
            safeApiCall {
                logRepository.generateReport(
                    token,
                    fromDate,
                    toDate
                )
            }
        )
    }

    private suspend fun <T> safeApiCall(
        call: suspend () -> Response<T>
    ): Resource<T> {
        return try {
            if (NetworkUtils.hasInternetConnection(getApplication())) {
                val response = call()

                if (response.isSuccessful) {
                    response.body()?.let {
                        return Resource.Success(it)
                    }
                }

                val errorBody = response.errorBody()?.string()

                Resource.Error(errorBody ?: response.message())
            } else {
                Resource.Error(getStringFromR(R.string.error_internet_connection))
            }
        } catch (t: Throwable) {
            when (t) {
                is IOException ->
                    Resource.Error(getStringFromR(R.string.error_io_dispatcher))

                else ->
                    Resource.Error(getStringFromR(R.string.error_conversion))
            }
        }
    }

    fun clearDownloadReportState(){
        _downloadReportState.value = null
    }

    private fun getStringFromR(id: Int) =
        getApplication< SeenApplication>().getString(id)
}