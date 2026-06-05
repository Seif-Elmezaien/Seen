package com.example.seen.ui.report.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.seen.datasource.repository.AuthRepository
import com.example.seen.datasource.repository.LogRepository
import com.example.seen.datasource.repository.MedicineRepository
import com.example.seen.datasource.repository.UserRepository
import com.example.seen.ui.authentication.viewmodel.AuthViewModel
import com.example.seen.ui.home.viewmodel.AddLogsViewModel

class ReportViewModelProviderFactory(
    val logRepository: LogRepository,
): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ReportViewModel( logRepository) as T
    }
}