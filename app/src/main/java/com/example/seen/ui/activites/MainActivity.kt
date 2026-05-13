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
import com.example.seen.util.Constants.Companion.NAV_ANIM_DURATION

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navHostFragment: NavHostFragment
    private lateinit var navController: NavController

    // Fragments that should hide the bottom bar
    private val fullScreenDestinations = setOf(
        R.id.addLogsFragment,
        R.id.addReminderFragment,
        R.id.reminderFragment,
        R.id.homeEntryFragment,
        R.id.onboardingContainerFragment
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if(checkToken() == null){
            goToAuthActivity()
            return
        }

        setUpSystemSettings()
        setUpBottomMenuNavController()

        binding.fabAddLogs.setOnClickListener {
//            onClickFabLogic()
            navController.navigate(R.id.addLogsFragment)
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

        binding.bottomNavigationView.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id in fullScreenDestinations) {
                // Delay hiding until transition is done
                binding.bottomAppBar.postDelayed({
                    // Guard: only hide if we're still on a fullscreen destination
                    // (user might have navigated back quickly)
                    if (navController.currentDestination?.id in fullScreenDestinations) {
                        binding.bottomAppBar.visibility = View.GONE
                        binding.fabAddLogs.visibility = View.GONE
                    }
                }, NAV_ANIM_DURATION)
            } else {
                binding.bottomAppBar.visibility = View.VISIBLE
                binding.fabAddLogs.visibility = View.VISIBLE
            }
        }
    }
}