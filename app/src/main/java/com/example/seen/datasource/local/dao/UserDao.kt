package com.example.seen.datasource.local.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.seen.domain.model.entites.User

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertUser(user: User) : Long

    @Query("SELECT * FROM User")
    fun getUser() : LiveData<User>

    @Delete
    suspend fun deleteUser(user: User)

}