package com.example.seen.datasource.repository

import com.example.seen.datasource.local.SeenDatabase
import com.example.seen.domain.model.entites.Medicine

class MedicineRepository(
    val db : SeenDatabase
) {
    // Medicines
    suspend fun insertMedicine(medicine: Medicine) =
        db.logDao.insertMedicine(medicine)

    fun getAllMedicines() =
        db.logDao.getAllMedicines()

    suspend fun deleteMedicine(medicine: Medicine) =
        db.logDao.deleteMedicine(medicine)

    suspend fun deleteAllMedicine() =
        db.logDao.deleteAllMedicine()
}