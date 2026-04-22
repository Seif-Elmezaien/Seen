package com.example.seen.datasource.local

import android.content.Context
import androidx.room.Database
import com.example.seen.domain.model.entites.Logs
import com.example.seen.domain.model.entites.RecordGlucose
import com.example.seen.domain.model.entites.RecordMeal
import com.example.seen.domain.model.entites.RecordMedication
import com.example.seen.domain.model.entites.User
import androidx.room.Room
import androidx.room.RoomDatabase


@Database(
    entities = [
        User::class,
        Logs::class,
        RecordGlucose::class,
        RecordMeal::class,
        RecordMedication::class
    ],
    version = 1
)
abstract class SeenDatabase : RoomDatabase() {
    abstract val seenDao: SeenDao


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
                "seen_db.db"
            ).build()
    }
}