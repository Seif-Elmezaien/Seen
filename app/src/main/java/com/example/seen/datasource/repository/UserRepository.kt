package com.example.seen.datasource.repository

import com.example.seen.datasource.local.SeenDatabase
import com.example.seen.domain.model.entites.Log
import com.example.seen.domain.model.entites.RecordGlucose
import com.example.seen.domain.model.entites.RecordMeal
import com.example.seen.domain.model.entites.RecordMedication
import com.example.seen.domain.model.entites.User

class UserRepository(
    val db : SeenDatabase
) {

    suspend fun upsertUser(user: User) =
        db.userDao.upsertUser(user)

    fun getUser() =
        db.userDao.getUser()

    suspend fun deleteUser(user: User) =
        db.userDao.deleteUser(user)
}