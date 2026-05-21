package com.example.seen.ui.home.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.example.seen.datasource.repository.LogRepository
import com.example.seen.datasource.repository.UserRepository
import com.example.seen.domain.model.entites.FullLog
import com.example.seen.domain.model.entites.Log
import com.example.seen.domain.model.entites.RecordGlucose
import com.example.seen.domain.model.entites.RecordMeal
import com.example.seen.domain.model.entites.RecordMedication
import kotlinx.coroutines.launch
import java.util.Calendar

class HomeViewModel(
    app: Application,
    private val userRepository: UserRepository,
    private val logRepository: LogRepository
) : AndroidViewModel(app) {

    val selectedDate = MutableLiveData(System.currentTimeMillis())

    val logs: LiveData<List<FullLog>> = selectedDate.switchMap { date ->
        val calendar = Calendar.getInstance().apply {
            timeInMillis = date
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val start = calendar.timeInMillis
        val end = start + 24 * 60 * 60 * 1000 - 1

        logRepository.getLogsByDate(start, end)
    }

    fun selectDate(date: Long) {
        selectedDate.value = date
    }

    fun getUser() =
        userRepository.getUser()

    fun deleteLog(log: Log) = viewModelScope.launch {
        logRepository.deleteLog(log)
    }

    fun insertLog(log: Log) = viewModelScope.launch {
        logRepository.insertLog(log)
    }

    fun insertRecordGlucose(recordGlucose: RecordGlucose) = viewModelScope.launch {
        logRepository.insertRecordGlucose(recordGlucose)
    }

    fun insertRecordMedication(recordMedication: RecordMedication) = viewModelScope.launch {
        logRepository.insertRecordMedication(recordMedication)
    }

    fun insertRecordMeal(recordMeal: RecordMeal) = viewModelScope.launch {
        logRepository.insertRecordMeal(recordMeal)
    }

    fun getLogById(logId: String) =
        logRepository.getLogById(logId)

    fun syncToServer(token: String) = viewModelScope.launch {
        logRepository.syncToServer(token)
    }
}