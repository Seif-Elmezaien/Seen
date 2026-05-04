package com.example.seen.ui.home.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.example.seen.datasource.repository.LogRepository
import com.example.seen.datasource.repository.UserRepository
import com.example.seen.domain.model.entites.FullLog
import com.example.seen.domain.model.entites.Log
import com.example.seen.domain.model.entites.RecordGlucose
import com.example.seen.domain.model.entites.RecordMeal
import com.example.seen.domain.model.entites.RecordMedication
import com.example.seen.domain.model.entites.User
import com.example.seen.util.SeenApplication
import kotlinx.coroutines.launch
import java.util.Calendar
import kotlin.random.Random

class HomeViewModel(
    app: Application,
    private val userRepository: UserRepository,
    private val logRepository: LogRepository
) : AndroidViewModel(app) {

    val selectedDate = MutableLiveData(System.currentTimeMillis())

    val logs: LiveData<List<FullLog>> = selectedDate.switchMap { date ->
        val calendar = Calendar.getInstance().apply {
            timeInMillis = date
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val start = calendar.timeInMillis
        val end = start + 24 * 60 * 60 * 1000 - 1

        android.util.Log.d("HomeViewModel", "Start: $start, End: $end")

        logRepository.getLogsByDate(start, end)
    }

    fun selectDate(date: Long) {
        selectedDate.value = date
    }

    //Temp Function Remove ya seif
    fun upsertUser() = viewModelScope.launch {
        userRepository.upsertUser(
            User(
                id = 1,
                first_name = "My Zozo",
                last_name = "Elmizayen",
                email = "saif.n.elmyzaien@gmail.com",
                diabetes_type = "Type2",
                password = "123456",
                gender = "male",
                phone = "01223755957",
                birthDate = "2004/10/25",
                insulin_therapy = "No",
                weight = 86.0,
                height = 180.0,
            )
        )
    }

    fun insertLog(log: Log) = viewModelScope.launch {
        logRepository.insertLog(log)
    }

    fun insertRecordGlucose(recordGlucose: RecordGlucose) = viewModelScope.launch {
        logRepository.insertRecordGlucose(recordGlucose)
    }

    fun insertRecordMeal(recordMeal: RecordMeal) = viewModelScope.launch {
        logRepository.insertRecordMeal(recordMeal)
    }

    fun insertRecordMedication(recordMedication: RecordMedication) = viewModelScope.launch {
        logRepository.insertRecordMedication(recordMedication)
    }

    fun generateMockData() = viewModelScope.launch {

        val now = System.currentTimeMillis()
        val dayMillis = 24 * 60 * 60 * 1000L

        val titles = listOf(
            "Morning Check", "Fasting Reading", "Before Breakfast",
            "After Breakfast", "Before Lunch", "After Lunch",
            "Afternoon Check", "Before Dinner", "After Dinner",
            "Evening Check", "Bedtime Reading", "Random Check",
            "Post Workout", "Late Night Reading", "Snack Time",
            "Midday Check", "Pre-Meal Check", "Post-Meal Check",
            "Routine Monitoring", "Daily Log Entry"
        )

        val descriptions = listOf(
            "Routine glucose monitoring",
            "Checking sugar level after meal",
            "Feeling a bit tired, checking levels",
            "Before eating to adjust insulin",
            "After eating to monitor spike",
            "Regular daily tracking",
            "Monitoring after physical activity",
            "Before sleep check",
            "Quick random check",
            "Tracking progress",
            "Post snack glucose check",
            "Ensuring stable levels",
            "Checking due to symptoms",
            "Normal daily record"
        )

        fun randomTime(base: Long): Long {
            val hour = (0..23).random()
            val minute = (0..59).random()
            return base + hour * 60 * 60 * 1000L + minute * 60 * 1000L
        }

        suspend fun insertLog(baseTime: Long, log_id: Int) {
            val createdAt = randomTime(baseTime)

            // 1) INSERT LOG FIRST
            val log = Log(
                log_id = log_id,
                log_title = titles.random(),
                log_description = descriptions.random(),
                created_at = createdAt,
                updated_at = createdAt
            )

            logRepository.insertLog(log)

            // 2) INSERT GLUCOSE (optional)
            if (Random.nextFloat() < 0.8f) {
                logRepository.insertRecordGlucose(
                    RecordGlucose(
                        reading_id = 0,
                        log_id = log_id,
                        glucose_level = (50..220).random().toFloat(),
                        reading_time = createdAt,
                        reading_type = listOf(
                            "Fasting",
                            "Before Meal",
                            "After Meal",
                            "Random"
                        ).random(),
                        notes = null,
                        a1c_estimation = null
                    )
                )
            }

            // 3) INSERT MEAL (optional)
            if (Random.nextFloat() < 0.5f) {
                logRepository.insertRecordMeal(
                    RecordMeal(
                        meal_id = 0,
                        log_id = log_id,
                        meal_name = listOf(
                            "Chicken", "Rice", "Salad",
                            "Burger", "Pasta", "Eggs"
                        ).random(),
                        meal_time = createdAt,
                        total_carb = (20..100).random().toFloat(),
                        total_calories = (200..800).random().toFloat(),
                        meal_type = listOf("Breakfast", "Lunch", "Dinner").random(),
                        notes = null
                    )
                )
            }

            // 4) INSERT MEDICATION (optional)
            if (Random.nextFloat() < 0.4f) {
                logRepository.insertRecordMedication(
                    RecordMedication(
                        medication_id = 0,
                        log_id = log_id,
                        medication_name = listOf(
                            "Metformin",
                            "Insulin",
                            "Glucophage"
                        ).random(),
                        medication_time = createdAt,
                        frequency = listOf(
                            "Once",
                            "Twice",
                            "Daily"
                        ).random(),
                        notes = null
                    )
                )
            }
        }

        // TODAY (6)
        repeat(6) {
            insertLog(now, it + 1)
        }

        // YESTERDAY (4)
        repeat(4) {
            insertLog(now - dayMillis, it + 7)
        }

        // DAY BEFORE YESTERDAY (10)
        repeat(10) {
            insertLog(now - 2 * dayMillis, it + 11)
        }
    }


    fun getUser() =
        userRepository.getUser()



    private fun getStringFromR(id: Int) =
        getApplication<SeenApplication>().getString(id)
}