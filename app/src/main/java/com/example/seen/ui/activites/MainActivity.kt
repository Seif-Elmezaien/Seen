package com.example.seen.ui.activites

import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.seen.R
import com.example.seen.databinding.ActivityMainBinding
import com.example.seen.ui.activites.AuthActivity

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if(checkToken() != null){
            Intent(this, AuthActivity::class.java).also {
                startActivity(it)
                finish()
            }
        }

        setUpSystemSettings()

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragmentContainerView)
                    as NavHostFragment
        val navController = navHostFragment.navController
        val bottomNav = binding.bottomNavigationView

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.homeFragment -> {
                    binding.bottomAppBar.visibility = View.VISIBLE
                    binding.fabAddLogs.visibility = View.VISIBLE
                }
            }
        }

    }

    private fun checkToken(): String? {
        val sharedPref = getSharedPreferences("Auth", MODE_PRIVATE)
        return sharedPref.getString("token", null)
    }

    private fun initializeDependencies(){

    }

    private fun setUpSystemSettings(){
        // screen rotation
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        // status bar color
        window.statusBarColor = Color.TRANSPARENT
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // bottomNavBar background error
        binding.bottomNavigationView.background = null
    }

    private fun setUpBottomMenuNavController(){
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragmentContainerView)
                    as NavHostFragment

        val navController = navHostFragment.navController

        val bottomNav = binding.bottomNavigationView
        bottomNav.setupWithNavController(navController)
    }



}