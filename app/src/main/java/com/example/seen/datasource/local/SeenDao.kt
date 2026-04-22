package com.example.seen.datasource.local

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.seen.domain.model.entites.FullLog
import com.example.seen.domain.model.entites.Logs
import com.example.seen.domain.model.entites.RecordGlucose
import com.example.seen.domain.model.entites.RecordMeal
import com.example.seen.domain.model.entites.RecordMedication
import com.example.seen.domain.model.entites.User
import kotlinx.coroutines.flow.Flow

@Dao
interface SeenDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertUser(user: User) : Long

    @Delete
    suspend fun deleteUser(user: User)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertLog(logs: Logs) : Long

    @Delete
    suspend fun deleteLog(logs: Logs)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRecordGlucose(recordGlucose: RecordGlucose): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRecordMeal(recordMeal: RecordMeal) : Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRecordMedication(recordMedication: RecordMedication): Long

    @Transaction()
    @Query("SELECT * FROM Logs ORDER BY created_at DESC")
    fun getAllLogs() : LiveData<List<FullLog>>

}