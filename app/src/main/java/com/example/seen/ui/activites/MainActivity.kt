package com.example.seen.ui.activites

import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.seen.R
import com.example.seen.databinding.ActivityMainBinding
import com.example.seen.ui.activites.AuthActivity

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navHostFragment: NavHostFragment
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if(checkToken() == null){
            goToAuthActivity()
        }

        setUpSystemSettings()
        setUpBottomMenuNavController()

        binding.fabAddLogs.setOnClickListener {
            onClickFabLogic()
        }
    }

    private fun checkToken(): String? {
        val sharedPref = getSharedPreferences("Auth", MODE_PRIVATE)
        return sharedPref.getString("token", null)
    }

    private fun goToAuthActivity() {
        Intent(this, AuthActivity::class.java).also {
            startActivity(it)
            finish()
        }
    }

    private fun setUpSystemSettings(){
        // screen rotation
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        // status bar color
        window.statusBarColor = Color.TRANSPARENT
        WindowCompat.setDecorFitsSystemWindows(window, false)

    }

    private fun setUpBottomMenuNavController(){

        // bottomNavBar background error
        binding.bottomNavigationView.background = null

        navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragmentContainerView)
                    as NavHostFragment

        navController = navHostFragment.navController

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.homeFragment -> {
                    binding.bottomAppBar.visibility = View.VISIBLE
                    binding.fabAddLogs.visibility = View.VISIBLE

                    // Setup BottomNavigationView
                    binding.bottomNavigationView.setupWithNavController(navController)
                }
            }
        }
    }

    private fun onClickFabLogic(){
        binding.bottomNavigationView.menu.setGroupCheckable(0, true, false)
        for (i in 0 until binding.bottomNavigationView.menu.size()) { binding.bottomNavigationView.menu.getItem(i).isChecked = false }
        binding.bottomNavigationView.menu.setGroupCheckable(0, true, true)
        navController.navigate(R.id.addLogsFragment)
    }



}