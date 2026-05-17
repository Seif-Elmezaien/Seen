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
import androidx.room.TypeConverters
import com.example.seen.datasource.local.dao.LogDao
import com.example.seen.datasource.local.dao.MedicineDao
import com.example.seen.datasource.local.dao.ReminderDao
import com.example.seen.datasource.local.dao.UserDao
import com.example.seen.domain.model.entites.Medicine
import com.example.seen.domain.model.entites.Reminder


@Database(
    entities = [
        User::class,
        Log::class,
        RecordGlucose::class,
        RecordMeal::class,
        RecordMedication::class,
        Medicine::class,
        Reminder::class  // add this
    ],
    version = 3
)
@TypeConverters(Converters :: class)
abstract class SeenDatabase : RoomDatabase() {
    abstract val userDao: UserDao
    abstract val logDao: LogDao
    abstract val reminderDao: ReminderDao
    abstract val medicineDao: MedicineDao




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
            )
            .fallbackToDestructiveMigration(false)
            .build()
    }
}