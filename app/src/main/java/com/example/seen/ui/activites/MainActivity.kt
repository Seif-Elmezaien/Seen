package com.example.seen.ui.activites

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.Network
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.seen.R
import com.example.seen.databinding.ActivityMainBinding
import com.example.seen.datasource.local.SeenDatabase
import com.example.seen.datasource.remote.RetrofitInstance
import com.example.seen.datasource.repository.LogRepository
import com.example.seen.domain.model.notification.FcmTokenRequest
import com.example.seen.ui.activites.AuthActivity
import com.example.seen.util.Constants.Companion.NAV_ANIM_DURATION
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navHostFragment: NavHostFragment
    private lateinit var navController: NavController
    lateinit var logRepository: LogRepository
    private var token : String? = null
    lateinit var sharedPref : SharedPreferences

    // Fragments that should hide the bottom bar
    private val fullScreenDestinations = setOf(
        R.id.addLogsFragment,
        R.id.logDetailFragment,
        R.id.addPostFragment,
        R.id.editLogsFragment,
        R.id.chatbotFragment,
        R.id.addReminderFragment,
        R.id.reminderFragment,
        R.id.homeEntryFragment,
        R.id.onboardingContainerFragment,
        R.id.postDetailsFragment,
        R.id.communitySearchFragment,
        R.id.profileFragment
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPref = getSharedPreferences("Auth", MODE_PRIVATE)
        token = checkToken()

        if(token == null){
            goToAuthActivity()
            return
        }

        val db = SeenDatabase(applicationContext)
        logRepository = LogRepository(db, sharedPref)

        setUpSystemSettings()
        setUpBottomMenuNavController()
        handleNotificationIntent(intent)
        observeConnectivity()
        sendTokenToServer()

        binding.fabAddLogs.setOnClickListener {
//            onClickFabLogic()
            navController.navigate(R.id.addLogsFragment)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(
                arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 1
            )
        }
    }

    private fun checkToken(): String? {
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

        val params = binding.fabAddLogs.layoutParams as CoordinatorLayout.LayoutParams
        params.behavior = null
        binding.fabAddLogs.layoutParams = params
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

    private fun observeConnectivity() {
        val connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager

        connectivityManager.registerDefaultNetworkCallback(object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                // triggers every time internet comes back
                lifecycleScope.launch {
                    logRepository.syncToServer("Bearer $token")
                    logRepository.syncFromServer("Bearer $token")
                }
            }
        })
    }

    private fun sendTokenToServer() {
        val api = RetrofitInstance.api

        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.e("FCM", "Failed to get token: ${task.exception}")
                return@addOnCompleteListener
            }

            val fcmToken = task.result

            // Always save the latest token
            sharedPref.edit().putString("fcm_token", fcmToken).apply()

            Log.d("FCM", "Sending token to server: $fcmToken")

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    api.updateFcmToken("Bearer $token", FcmTokenRequest(fcmToken))
                } catch (e: Exception) {
                    Log.e("FCM", "Failed to send token: ${e.message}")
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleNotificationIntent(intent)
    }

    private fun handleNotificationIntent(intent: Intent?) {
        if (intent?.getStringExtra("navigate_to") == "notifications") {
            // wait for nav controller to be ready
            binding.root.post {
                navController.navigate(R.id.notificationFragment)
            }
        }
    }
}