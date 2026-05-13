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
import com.example.seen.domain.model.entites.Medicine
import com.example.seen.domain.model.entites.RecordGlucose
import com.example.seen.domain.model.entites.RecordMeal
import com.example.seen.domain.model.entites.RecordMedication
import com.example.seen.domain.model.entites.Reminder

@Dao
interface MedicineDao {

    // insert Medicine
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMedicine(medicine: Medicine) : Long

    @Query("SELECT * FROM medicine ORDER BY medicine_name ASC")
    fun getAllMedicines() : LiveData<List<Medicine>>

    @Delete
    suspend fun deleteMedicine(medicine: Medicine)

    @Query("DELETE FROM medicine")
    suspend fun deleteAllMedicine()
}