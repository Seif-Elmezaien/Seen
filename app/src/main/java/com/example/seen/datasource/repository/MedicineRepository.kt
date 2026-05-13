package com.example.seen.datasource.repository

import com.example.seen.datasource.local.SeenDatabase
import com.example.seen.domain.model.entites.Medicine

class MedicineRepository(
    val db : SeenDatabase
) {
    // Medicines
    suspend fun insertMedicine(medicine: Medicine) =
        db.medicineDao.insertMedicine(medicine)

    fun getAllMedicines() =
        db.medicineDao.getAllMedicines()

    suspend fun deleteMedicine(medicine: Medicine) =
        db.medicineDao.deleteMedicine(medicine)

    suspend fun deleteAllMedicine() =
        db.medicineDao.deleteAllMedicine()
}