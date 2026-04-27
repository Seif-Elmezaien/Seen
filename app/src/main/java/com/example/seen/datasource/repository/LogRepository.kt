package com.example.seen.datasource.repository

import com.example.seen.datasource.local.SeenDatabase
import com.example.seen.domain.model.entites.Log
import com.example.seen.domain.model.entites.RecordGlucose
import com.example.seen.domain.model.entites.RecordMeal
import com.example.seen.domain.model.entites.RecordMedication

class LogRepository(
    val db : SeenDatabase
) {

    // Logs
    suspend fun insertLog(log: Log) =
        db.logDao.insertLog(log)

    suspend fun deleteLog(log: Log) =
        db.logDao.deleteLog(log)

    // RecordGlucose
    suspend fun insertRecordGlucose(recordGlucose: RecordGlucose) =
        db.logDao.insertRecordGlucose(recordGlucose)

    suspend fun updateRecordGlucose(recordGlucose: RecordGlucose) =
        db.logDao.updateRecordGlucose(recordGlucose)

    suspend fun deleteRecordGlucose(recordGlucose: RecordGlucose) =
        db.logDao.deleteRecordGlucose(recordGlucose)

    // RecordMeal
    suspend fun insertRecordMeal(recordMeal: RecordMeal) =
        db.logDao.insertRecordMeal(recordMeal)

    suspend fun updateRecordMeal(recordMeal: RecordMeal) =
        db.logDao.updateRecordMeal(recordMeal)

    suspend fun deleteRecordMeal(recordMeal: RecordMeal) =
        db.logDao.deleteRecordMeal(recordMeal)


    // RecordMedication
    suspend fun insertRecordMedication(recordMedication: RecordMedication) =
        db.logDao.insertRecordMedication(recordMedication)

    suspend fun updateRecordMedication(recordMedication: RecordMedication) =
        db.logDao.updateRecordMedication(recordMedication)

    suspend fun deleteRecordMedication(recordMedication: RecordMedication) =
        db.logDao.deleteRecordMedication(recordMedication)

    // get all Logs
    fun getAllLogs() =
        db.logDao.getAllLogs()

    fun getLogsByDate(startOfDay: Long, endOfDay: Long) =
        db.logDao.getLogByDate(startOfDay, endOfDay)
}