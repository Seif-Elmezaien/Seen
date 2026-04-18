package com.example.seen.ui

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.seen.databinding.ActivityMainBinding
import com.example.seen.ui.authentication.AuthActivity

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        val token = checkToken()

        if(token != null){
            Intent(this, AuthActivity::class.java).also {
                startActivity(it)
            }
        }
    }

    private fun checkToken(): String? {
        val sharedPref = getSharedPreferences("token", MODE_PRIVATE)
        return sharedPref.getString("token", null)
    }

}