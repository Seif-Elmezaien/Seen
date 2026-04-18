package com.example.seen.ui.authentication

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.seen.databinding.AuthMainBinding
import com.example.seen.datasource.repository.AuthRepository
import com.example.seen.ui.authentication.viewmodel.AuthViewModel
import com.example.seen.ui.authentication.viewmodel.AuthViewModelProviderFactory

class AuthActivity : AppCompatActivity() {

    lateinit var binding: AuthMainBinding
    lateinit var viewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AuthMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //to make the app in portrait mode only
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        val authRepository = AuthRepository()
        val viewModelProviderFactory = AuthViewModelProviderFactory(application, authRepository)
        viewModel = ViewModelProvider(this, viewModelProviderFactory).get(AuthViewModel::class.java)
    }
}