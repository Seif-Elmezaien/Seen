package com.example.seen.datasource.local.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.seen.domain.model.entites.FullLog
import com.example.seen.domain.model.entites.GraphPoint
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

    // Analysis

    @Query("""
        SELECT COUNT(*)
        FROM logs l
        INNER JOIN RecordGlucose rg ON l.log_id = rg.log_id
        WHERE l.logged_at BETWEEN :startDate AND :endDate
""")
    suspend fun getGlucoseLogsCount(
        startDate: Long,
        endDate: Long,
    ): Int

    @Query("""
    SELECT AVG(rg.glucose_level)
    FROM logs l
    INNER JOIN RecordGlucose rg ON l.log_id = rg.log_id
    WHERE l.logged_at BETWEEN :startDate AND :endDate
""")
    suspend fun getAverageGlucose(startDate: Long, endDate: Long): Float?

    @Transaction
    @Query("""
    SELECT * FROM logs
    WHERE log_id = (
        SELECT rg.log_id
        FROM RecordGlucose rg
        INNER JOIN logs l ON l.log_id = rg.log_id
        WHERE l.logged_at BETWEEN :startDate AND :endDate
        ORDER BY rg.glucose_level ASC
        LIMIT 1
    )
""")
    suspend fun getLowestGlucoseLog(startDate: Long, endDate: Long): FullLog?

    @Transaction
    @Query("""
    SELECT * FROM logs
    WHERE log_id = (
        SELECT rg.log_id
        FROM RecordGlucose rg
        INNER JOIN logs l ON l.log_id = rg.log_id
        WHERE l.logged_at BETWEEN :startDate AND :endDate
        ORDER BY rg.glucose_level DESC
        LIMIT 1
    )
   """)
    suspend fun getHighestGlucoseLog(startDate: Long, endDate: Long): FullLog?

    @Query("""
    SELECT
        rg.glucose_level as glucoseValue,
        rg.reading_type as readingType,
        l.logged_at as loggedAt,
        l.log_id as logId
    FROM logs l
    INNER JOIN RecordGlucose rg
        ON l.log_id = rg.log_id
    WHERE l.logged_at BETWEEN :startDate AND :endDate
    AND (:readingType IS NULL OR rg.reading_type = :readingType)
    ORDER BY l.logged_at ASC
""")
    fun getGraphData(
        startDate: Long,
        endDate: Long,
        readingType: String?
    ): LiveData<List<GraphPoint>>


    @Transaction
    @Query("SELECT * FROM logs WHERE is_synced = 0")
    suspend fun getUnsyncedFullLogs(): List<FullLog>

    @Query("UPDATE logs SET is_synced = 1 WHERE log_id = :logId")
    suspend fun markSynced(logId: String)
}