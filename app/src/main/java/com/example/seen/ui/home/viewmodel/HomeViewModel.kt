package com.example.seen.ui.home.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.seen.datasource.repository.LogRepository
import com.example.seen.datasource.repository.UserRepository
import com.example.seen.domain.model.entites.User
import com.example.seen.util.SeenApplication
import kotlinx.coroutines.launch

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

    fun getLog() =
        logRepository.getAllLogs()



    private fun getStringFromR(id: Int) =
        getApplication<SeenApplication>().getString(id)
}