package com.example.seen.datasource.repository

import com.example.seen.datasource.local.SeenDatabase
import com.example.seen.domain.model.entites.Logs
import com.example.seen.domain.model.entites.RecordGlucose
import com.example.seen.domain.model.entites.RecordMeal
import com.example.seen.domain.model.entites.RecordMedication
import com.example.seen.domain.model.entites.User

class MainRepository(
    val db : SeenDatabase
) {

    suspend fun upsertUser(user: User) =
        db.seenDao.upsertUser(user)

    suspend fun deleteUser(user: User) =
        db.seenDao.deleteUser(user)

    suspend fun upsertLog(logs: Logs) =
        db.seenDao.upsertLog(logs)

    suspend fun deleteLog(log: Logs) =
        db.seenDao.deleteLog(log)

    suspend fun upsertRecordGlucose(recordGlucose: RecordGlucose) =
        db.seenDao.upsertRecordGlucose(recordGlucose)

    suspend fun upsertRecordMeal(recordMeal: RecordMeal) =
        db.seenDao.upsertRecordMeal(recordMeal)

    suspend fun upsertRecordMedication(recordMedication: RecordMedication) =
        db.seenDao.upsertRecordMedication(recordMedication)

    fun getAllLogs() =
        db.seenDao.getAllLogs()

}