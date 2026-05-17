package com.example.seen.datasource.repository

import com.example.seen.datasource.local.SeenDatabase
import com.example.seen.datasource.remote.RetrofitInstance
import com.example.seen.datasource.remote.SeenAPI
import com.example.seen.domain.model.entites.Log
import com.example.seen.domain.model.entites.RecordGlucose
import com.example.seen.domain.model.entites.RecordMeal
import com.example.seen.domain.model.entites.RecordMedication
import com.example.seen.domain.model.logs.CombinedLogRequestResponse
import com.example.seen.domain.model.logs.GlucoseRequest
import com.example.seen.domain.model.logs.MealRequest
import com.example.seen.domain.model.logs.MedicationRequest
import com.example.seen.util.toFormattedDate

class LogRepository(
    val db : SeenDatabase,
) {

    // Logs
    suspend fun insertLog(log: Log) =
        db.logDao.insertLog(log)

    suspend fun deleteLog(log: Log) =
        db.logDao.deleteLog(log)

    // RecordGlucose
    suspend fun insertRecordGlucose(recordGlucose: RecordGlucose) =
        db.logDao.insertRecordGlucose(recordGlucose)

    suspend fun deleteRecordGlucose(recordGlucose: RecordGlucose) =
        db.logDao.deleteRecordGlucose(recordGlucose)

    // RecordMeal
    suspend fun insertRecordMeal(recordMeal: RecordMeal) =
        db.logDao.insertRecordMeal(recordMeal)

    suspend fun deleteRecordMeal(recordMeal: RecordMeal) =
        db.logDao.deleteRecordMeal(recordMeal)


    // RecordMedication
    suspend fun insertRecordMedication(recordMedication: RecordMedication) =
        db.logDao.insertRecordMedication(recordMedication)

    suspend fun deleteRecordMedication(recordMedication: RecordMedication) =
        db.logDao.deleteRecordMedication(recordMedication)

    // get all Logs
    fun getAllLogs() =
        db.logDao.getAllLogs()

    fun getLogsByDate(startOfDay: Long, endOfDay: Long) =
        db.logDao.getLogByDate(startOfDay, endOfDay)

    // ↓ Sync
    suspend fun syncToServer(token : String) {
        val unsyncedLogs = db.logDao.getUnsyncedFullLogs()
        unsyncedLogs.forEach { fullLog ->
            try {
                val response = RetrofitInstance.api.uploadLog(
                    token,
                    CombinedLogRequestResponse(
                        log_id = fullLog.log.log_id,
                        log_title = fullLog.log.log_title,
                        log_description = fullLog.log.log_description,
                        logged_at = fullLog.log.logged_at.toFormattedDate(),
                        recordGlucose = fullLog.glucose?.let {
                            GlucoseRequest(
                                glucose_level = it.glucose_level,
                                reading_type = it.reading_type,
                                a1c_estimation = it.a1c_estimation,
                                notes = it.notes
                            )
                        },
                        recordMeal = fullLog.meal?.let {
                            MealRequest(
                                meal_type = it.meal_type,
                                meal_description = it.meal_description,
                                total_carb = it.total_carb,
                                total_calories = it.total_calories,
                                notes = it.notes
                            )
                        },
                        recordMedication = fullLog.medication?.let {
                            MedicationRequest(
                                medications = it.medications,
                                notes = it.notes
                            )
                        }
                    )
                )
                if (response.isSuccessful) {
                    db.logDao.markSynced(fullLog.log.log_id)
                }
            } catch (e: Exception) {
                // no internet, skip and retry next time
            }
        }
    }

}