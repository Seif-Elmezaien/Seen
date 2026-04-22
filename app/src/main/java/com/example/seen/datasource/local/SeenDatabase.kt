package com.example.seen.datasource.local

import android.content.Context
import androidx.room.Database
import com.example.seen.domain.model.entites.Log
import com.example.seen.domain.model.entites.RecordGlucose
import com.example.seen.domain.model.entites.RecordMeal
import com.example.seen.domain.model.entites.RecordMedication
import com.example.seen.domain.model.entites.User
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.seen.datasource.local.dao.LogDao
import com.example.seen.datasource.local.dao.UserDao


@Database(
    entities = [
        User::class,
        Log::class,
        RecordGlucose::class,
        RecordMeal::class,
        RecordMedication::class
    ],
    version = 1
)
abstract class SeenDatabase : RoomDatabase() {
    abstract val userDao: UserDao
    abstract val logDao: LogDao



    companion object {
        @Volatile
        private var instance: SeenDatabase? = null
        private val LOCK = Any()

        operator fun invoke(context: Context) = instance ?: synchronized(LOCK) {
            instance ?: createDatabase(context).also { instance = it }
        }

        private fun createDatabase(context: Context) =
            Room.databaseBuilder(
                context.applicationContext,
                SeenDatabase::class.java,
                "seen.db"
            ).build()
    }
}