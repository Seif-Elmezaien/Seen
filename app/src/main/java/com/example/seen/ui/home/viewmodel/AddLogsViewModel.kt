package com.example.seen.ui.home.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.seen.datasource.repository.LogRepository
import com.example.seen.domain.model.entites.Log
import com.example.seen.domain.model.entites.Medicine
import com.example.seen.domain.model.entites.RecordGlucose
import com.example.seen.domain.model.entites.RecordMeal
import com.example.seen.domain.model.entites.RecordMedication

class AddLogsViewModel(
    app: Application,
    private val logRepository: LogRepository
) : AndroidViewModel(app) {

    suspend fun insertLog(log: Log) =
        logRepository.insertLog(log)

    suspend fun insertRecordGlucose(recordGlucose: RecordGlucose) =
        logRepository.insertRecordGlucose(recordGlucose)

    suspend fun insertRecordMeal(recordMeal: RecordMeal) =
        logRepository.insertRecordMeal(recordMeal)

    suspend fun insertRecordMedication(recordMedication: RecordMedication) =
        logRepository.insertRecordMedication(recordMedication)

    fun getAllMedicines() =
        logRepository.getAllMedicines()

    suspend fun insertMedicine(medicine: Medicine) =
        logRepository.insertMedicine(medicine)

    suspend fun deleteAllMedicine() =
        logRepository.deleteAllMedicine()

}