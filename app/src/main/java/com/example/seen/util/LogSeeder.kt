package com.example.seen.util

import com.example.seen.datasource.local.dao.LogDao
import com.example.seen.domain.model.entites.Log
import com.example.seen.domain.model.entites.RecordGlucose
import com.example.seen.domain.model.entites.RecordMeal
import com.example.seen.util.Constants.Companion.AFTER_MEAL
import com.example.seen.util.Constants.Companion.BEFORE_MEAL
import com.example.seen.util.Constants.Companion.FASTING
import com.example.seen.util.Constants.Companion.RANDOM
import java.util.Calendar
import java.util.UUID
import kotlin.random.Random

class LogSeeder {
    companion object DatabaseSeeder {

        private val readingTypes = listOf(FASTING, BEFORE_MEAL, AFTER_MEAL, RANDOM)

        private val mealTypes = listOf("BREAKFAST", "LUNCH", "DINNER", "SNACK")

        private val logTitles = mapOf(
            FASTING     to "Fasting Reading",
            BEFORE_MEAL to "Pre-meal Check",
            AFTER_MEAL  to "Post-meal Check",
            RANDOM      to "Random Check"
        )

        // Realistic glucose ranges per reading type (mg/dL)
        private val glucoseRanges = mapOf(
            FASTING     to 70..130,
            BEFORE_MEAL to 80..140,
            AFTER_MEAL  to 100..200,
            RANDOM      to 70..180
        )

        suspend fun seed(dao: LogDao) {
            val random = Random(System.currentTimeMillis())
            val calendar = Calendar.getInstance()

            // Start from 4 months ago
            val endTime = calendar.timeInMillis
            calendar.add(Calendar.MONTH, -4)
            val startTime = calendar.timeInMillis

            var current = startTime

            while (current < endTime) {

                // 2-4 logs per day
                val logsPerDay = random.nextInt(2, 5)

                repeat(logsPerDay) { index ->

                    // Spread logs across the day (morning, noon, evening, night)
                    val hourOffsets = listOf(7, 12, 18, 22)
                    val hour = hourOffsets.getOrElse(index) { random.nextInt(6, 23) }

                    val logTime = Calendar.getInstance().apply {
                        timeInMillis = current
                        set(Calendar.HOUR_OF_DAY, hour)
                        set(Calendar.MINUTE, random.nextInt(0, 59))
                        set(Calendar.SECOND, 0)
                    }.timeInMillis

                    val readingType = readingTypes[index % readingTypes.size]
                    val logId = UUID.randomUUID().toString()

                    // Insert Log
                    val log = Log(
                        log_id = logId,
                        log_title = logTitles[readingType] ?: "Log",
                        log_description = "Seeded log for $readingType",
                        logged_at = logTime,
                        updated_at = logTime,
                        is_synced = false
                    )
                    dao.insertLog(log)

                    // Insert Glucose
                    val glucoseLevel = glucoseRanges[readingType]?.let {
                        random.nextInt(it.first, it.last)
                    } ?: random.nextInt(70, 200)

                    val glucose = RecordGlucose(
                        reading_id = 0, // autoGenerate
                        log_id = logId,
                        reading_type = readingType,
                        glucose_level = glucoseLevel,
                        notes = null
                    )
                    dao.insertRecordGlucose(glucose)

                    // 30% chance to also add a meal log
                    if (random.nextFloat() < 0.3f) {
                        val meal = RecordMeal(
                            meal_id = 0,
                            log_id = logId,
                            meal_type = mealTypes[random.nextInt(mealTypes.size)],
                            meal_description = "Seeded meal entry",
                            total_carb = random.nextInt(20, 120),
                            total_calories = random.nextInt(200, 800),
                            notes = null
                        )
                        dao.insertRecordMeal(meal)
                    }
                }

                // Move to next day
                current += 24 * 60 * 60 * 1000L
            }
        }
    }
}