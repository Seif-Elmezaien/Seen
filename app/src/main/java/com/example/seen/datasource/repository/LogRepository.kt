package com.example.seen.datasource.repository

import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.lifecycle.LiveData
import com.example.seen.datasource.local.SeenDatabase
import com.example.seen.datasource.remote.RetrofitInstance
import com.example.seen.domain.model.entites.DeletedLog
import com.example.seen.domain.model.entites.FullLog
import com.example.seen.domain.model.entites.GraphPoint
import com.example.seen.domain.model.entites.Log
import com.example.seen.domain.model.entites.RecordGlucose
import com.example.seen.domain.model.entites.RecordMeal
import com.example.seen.domain.model.entites.RecordMedication
import com.example.seen.domain.model.entites.SelectedMedication
import com.example.seen.domain.model.logs.LogRequest
import com.example.seen.domain.model.logs.GlucoseRequest
import com.example.seen.domain.model.logs.MealRequest
import com.example.seen.domain.model.logs.MedicationRequest
import com.example.seen.util.toFormattedDate
import com.example.seen.util.toTimestamp
import okhttp3.ResponseBody

class LogRepository(
    val db : SeenDatabase,
    val prefs: SharedPreferences? = null
) {

    // Logs
    suspend fun insertLog(log: Log) {
        db.logDao.insertLog(log)
        db.deletedLogDao.remove(log.log_id)
    }

    suspend fun deleteLog(log: Log) {
        db.logDao.deleteLog(log)                          // delete locally
        db.deletedLogDao.insert(DeletedLog(log.log_id))   // queue for server
    }

    // RecordGlucose
    suspend fun insertRecordGlucose(recordGlucose: RecordGlucose) =
        db.logDao.insertRecordGlucose(recordGlucose)

    // RecordMeal
    suspend fun insertRecordMeal(recordMeal: RecordMeal) =
        db.logDao.insertRecordMeal(recordMeal)

    // RecordMedication
    suspend fun insertRecordMedication(recordMedication: RecordMedication) =
        db.logDao.insertRecordMedication(recordMedication)

    // get all Logs
    fun getAllLogs() =
        db.logDao.getAllLogs()

    fun getLogsByDate(startOfDay: Long, endOfDay: Long) =
        db.logDao.getLogByDate(startOfDay, endOfDay)

    fun getLogById(logId: String) =
        db.logDao.getLogById(logId)


    suspend fun getGlucoseLogsCount(startDate: Long, endDate: Long): Int =
        db.logDao.getGlucoseLogsCount(startDate, endDate)

    suspend fun getAverageGlucose(startDate: Long, endDate: Long): Float? =
        db.logDao.getAverageGlucose(startDate, endDate)

    suspend fun getLowestGlucoseLog(startDate: Long, endDate: Long): FullLog? =
        db.logDao.getLowestGlucoseLog(startDate, endDate)

    suspend fun getHighestGlucoseLog(startDate: Long, endDate: Long): FullLog? =
        db.logDao.getHighestGlucoseLog(startDate, endDate)

    fun getGraphData(startDate: Long, endDate: Long, readingType: String?): LiveData<List<GraphPoint>> =
        db.logDao.getGraphData(startDate, endDate, readingType)

    suspend fun generateReport(token: String, startDate: Long, endDate: Long) =
        RetrofitInstance.api.generateReport(token, startDate.toFormattedDate(), endDate.toFormattedDate())

    // ↓ Sync
    suspend fun syncToServer(token : String) {
        val unsyncedLogs = db.logDao.getUnsyncedFullLogs()
        unsyncedLogs.forEach { fullLog ->
            val selectedMedication = fullLog.medication?.medications?.map { it.medication_name }
            try {
                val response = RetrofitInstance.api.uploadLog(
                    token,
                    LogRequest(
                        log_id = fullLog.log.log_id,
                        log_title = fullLog.log.log_title,
                        log_description = fullLog.log.log_description,
                        logged_at = fullLog.log.logged_at.toFormattedDate(),
                        record_glucose = fullLog.glucose?.let {
                            GlucoseRequest(
                                glucose_level = it.glucose_level,
                                reading_type = it.reading_type,
                                a1c_estimation = it.a1c_estimation,
                                notes = it.notes
                            )
                        },
                        record_meal = fullLog.meal?.let {
                            MealRequest(
                                meal_type = it.meal_type,
                                meal_description = it.meal_description,
                                total_carb = it.total_carb,
                                total_calories = it.total_calories,
                                notes = it.notes
                            )
                        },
                        record_medication = fullLog.medication?.let {
                            MedicationRequest(
                                medications = selectedMedication!!,
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

        val pendingDeletes = db.deletedLogDao.getAll()
        pendingDeletes.forEach { deleted ->
            try {
                val response = RetrofitInstance.api.deleteLog(token, deleted.log_id)
                if (response.isSuccessful || response.code() == 404) {
                    db.deletedLogDao.remove(deleted.log_id)
                }

            } catch (e: Exception) {
                // offline, retry next time
            }
        }
    }

    suspend fun syncFromServer(token: String) {
        try {
            val lastSync = prefs?.getLong("last_sync", 0L)
            val updatedSince = if (lastSync == 0L) null else lastSync?.toFormattedDate()

            val response = RetrofitInstance.api.syncLogs(token, updatedSince)
            if (response.isSuccessful) {

                val newLogs = response.body()?.data?.upserted_logs ?: return
                val deletedLogs = response.body()?.data?.deleted_log_ids ?: emptyList()

                newLogs.forEach { serverLog ->

                    db.logDao.insertLog(
                        Log(
                            log_id = serverLog.log_id,
                            log_title = serverLog.log_title ?: "",
                            log_description = serverLog.log_description ?: "",
                            logged_at = serverLog.logged_at.toTimestamp(),
                            is_synced = true
                        )
                    )

                    serverLog.record_glucose?.let {
                        db.logDao.insertRecordGlucose(
                            RecordGlucose(
                                reading_id = 0,
                                log_id = serverLog.log_id,
                                reading_type = it.reading_type,
                                glucose_level = it.glucose_level,
                                a1c_estimation = it.a1c_estimation,
                                notes = it.notes
                            )
                        )
                    }

                    serverLog.record_meal?.let {
                        db.logDao.insertRecordMeal(
                            RecordMeal(
                                meal_id = 0,
                                log_id = serverLog.log_id,
                                meal_type = it.meal_type,
                                meal_description = it.meal_description ?: "",
                                total_carb = it.total_carb,
                                total_calories = it.total_calories,
                                notes = it.notes
                            )
                        )
                    }

                    serverLog.record_medication?.let {
                        val selectedMedication = it.medications.map { name ->
                            SelectedMedication(name)
                        }
                        db.logDao.insertRecordMedication(
                            RecordMedication(
                                medication_id = 0,
                                log_id = serverLog.log_id,
                                medications = selectedMedication,
                                notes = it.notes
                            )
                        )
                    }
                }

                deletedLogs.forEach {
                    db.logDao.deleteLog(it)
                }

                // save last sync time after successful fetch
                prefs?.edit { putLong("last_sync", System.currentTimeMillis()) }
            }
        } catch (e: Exception) {
            // no internet, skip
            android.util.Log.e("logrepo", "error: ${e.message}", e)
        }
    }

}