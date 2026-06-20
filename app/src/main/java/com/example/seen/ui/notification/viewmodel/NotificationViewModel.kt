package com.example.seen.ui.notification.viewmodel

import android.app.Application
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.example.seen.datasource.repository.LogRepository
import com.example.seen.datasource.repository.NotificationRepository
import com.example.seen.datasource.repository.UserRepository
import com.example.seen.domain.model.entites.FullLog
import com.example.seen.domain.model.entites.Log
import com.example.seen.domain.model.entites.RecordGlucose
import com.example.seen.domain.model.entites.RecordMeal
import com.example.seen.domain.model.entites.RecordMedication
import com.example.seen.util.isOnline
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

class NotificationViewModel(
    app: Application,
    private val notificationRepository: NotificationRepository
) : AndroidViewModel(app) {

}