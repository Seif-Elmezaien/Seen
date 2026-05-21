package com.example.seen.datasource.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.seen.domain.model.entites.DeletedLog

@Dao
interface DeletedLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(deletedLog: DeletedLog)

    @Query("SELECT * FROM deleted_logs")
    suspend fun getAll(): List<DeletedLog>

    @Query("DELETE FROM deleted_logs WHERE log_id = :logId")
    suspend fun remove(logId: String)
}