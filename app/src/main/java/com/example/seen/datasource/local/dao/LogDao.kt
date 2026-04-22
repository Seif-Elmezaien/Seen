package com.example.seen.datasource.local.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.seen.domain.model.entites.FullLog
import com.example.seen.domain.model.entites.Log
import com.example.seen.domain.model.entites.RecordGlucose
import com.example.seen.domain.model.entites.RecordMeal
import com.example.seen.domain.model.entites.RecordMedication

@Dao
interface LogDao {

    //Logs
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertLog(log: Log) : Long

    @Delete
    suspend fun deleteLog(log: Log)

    //RecordGlucose
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertRecordGlucose(recordGlucose: RecordGlucose): Long

    @Update()
    suspend fun updateRecordGlucose(recordGlucose: RecordGlucose)

    @Delete
    suspend fun deleteRecordGlucose(recordGlucose: RecordGlucose)

    //RecordMeal
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertRecordMeal(recordMeal: RecordMeal) : Long

    @Update()
    suspend fun updateRecordMeal(recordMeal: RecordMeal)

    @Delete
    suspend fun deleteRecordMeal(recordMeal: RecordMeal)

    //RecordMedication
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertRecordMedication(recordMedication: RecordMedication): Long

    @Update()
    suspend fun updateRecordMedication(recordMedication: RecordMedication)

    @Delete
    suspend fun deleteRecordMedication(recordMedication: RecordMedication)

    //get All Logs
    @Transaction()
    @Query("SELECT * FROM Log ORDER BY created_at DESC")
    fun getAllLogs() : LiveData<List<FullLog>>

}