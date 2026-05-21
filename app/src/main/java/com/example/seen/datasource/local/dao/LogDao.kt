package com.example.seen.datasource.local.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.seen.domain.model.entites.FullLog
import com.example.seen.domain.model.entites.Log
import com.example.seen.domain.model.entites.RecordGlucose
import com.example.seen.domain.model.entites.RecordMeal
import com.example.seen.domain.model.entites.RecordMedication

@Dao
interface LogDao {

    //Logs
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: Log)

    @Delete
    suspend fun deleteLog(log: Log)

    @Query("Delete FROM logs WHERE log_id = :logId")
    suspend fun deleteLog(logId: String)

    //RecordGlucose
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecordGlucose(recordGlucose: RecordGlucose)

    //RecordMeal
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecordMeal(recordMeal: RecordMeal)

    //RecordMedication
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecordMedication(recordMedication: RecordMedication)

    //get All Logs
    @Transaction()
    @Query("SELECT * FROM logs ORDER BY logged_at DESC")
    fun getAllLogs() : LiveData<List<FullLog>>

    @Transaction()
    @Query("SELECT * FROM logs WHERE logged_at BETWEEN :startOfDay AND :endOfDate ORDER BY logged_at DESC")
    fun getLogByDate(startOfDay: Long, endOfDate: Long) : LiveData<List<FullLog>>

    @Transaction
    @Query("SELECT * FROM logs WHERE log_id = :logId")
    fun getLogById(logId: String): LiveData<FullLog>

    @Transaction
    @Query("SELECT * FROM logs WHERE is_synced = 0")
    suspend fun getUnsyncedFullLogs(): List<FullLog>

    @Query("UPDATE logs SET is_synced = 1 WHERE log_id = :logId")
    suspend fun markSynced(logId: String)
}