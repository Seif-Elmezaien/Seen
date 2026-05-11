package com.example.seen.ui.home.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.seen.datasource.repository.LogRepository
import com.example.seen.domain.model.entites.Log
import com.example.seen.domain.model.entites.Medicine
import com.example.seen.domain.model.entites.RecordGlucose
import com.example.seen.domain.model.entites.RecordMeal
import com.example.seen.domain.model.entites.RecordMedication
import kotlinx.coroutines.launch

class AddLogsViewModel(
    app: Application,
    private val logRepository: LogRepository
) : AndroidViewModel(app) {

    suspend fun insertLog(log: Log) =
        logRepository.insertLog(log)

    suspend fun insertRecordGlucose(recordGlucose: RecordGlucose) =
        logRepository.insertRecordGlucose(recordGlucose)

    suspend fun insertRecordMedication(recordMedication: RecordMedication) =
        logRepository.insertRecordMedication(recordMedication)

    suspend fun insertRecordMeal(recordMeal: RecordMeal) =
        logRepository.insertRecordMeal(recordMeal)

    fun getAllMedicines() =
        logRepository.getAllMedicines()

    fun insertMedicine(medicine: Medicine) = viewModelScope.launch {
        logRepository.insertMedicine(medicine)
    }

}