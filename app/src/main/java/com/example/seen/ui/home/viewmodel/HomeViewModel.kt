package com.example.seen.ui.home.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.seen.datasource.repository.LogRepository
import com.example.seen.datasource.repository.UserRepository
import com.example.seen.domain.model.entites.FullLog
import com.example.seen.domain.model.entites.Log
import com.example.seen.domain.model.entites.User
import com.example.seen.util.SeenApplication
import kotlinx.coroutines.launch
import java.util.Calendar

class HomeViewModel(
    app: Application,
    private val userRepository: UserRepository,
    private val logRepository: LogRepository
) : AndroidViewModel(app) {

    //Temp Function Remove ya seif
    fun upsertUser() = viewModelScope.launch {
        userRepository.upsertUser(
            User(
                id = 1,
                first_name = "Seif",
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


    fun getUser() =
        userRepository.getUser()

    fun getLogByDate(date : Long) : LiveData<List<FullLog>> {

        val calendar = Calendar.getInstance().apply {
            timeInMillis = date
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startOfDay = calendar.timeInMillis

        val endOfDay = startOfDay + 24 * 60 * 60 * 1000 - 1   // +23:59:59.999

        return logRepository.getLogsByDate(startOfDay, endOfDay)

    }



    private fun getStringFromR(id: Int) =
        getApplication<SeenApplication>().getString(id)
}