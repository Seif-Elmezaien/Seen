package com.example.seen.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.seen.R
import com.example.seen.datasource.remote.RetrofitInstance
import com.example.seen.domain.model.notification.FcmTokenRequest
import com.example.seen.ui.activites.MainActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // send new token to your Laravel backend
        sendTokenToServer(token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val title = remoteMessage.notification?.title ?: remoteMessage.data["title"] ?: "Seen"
        val body  = remoteMessage.notification?.body  ?: remoteMessage.data["body"]  ?: ""

        showNotification(title, body)
    }

    private fun showNotification(title: String, body: String) {
        val channelId = "seen_channel"

        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)  // 👈 add your icon
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // create channel for Android 8+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Seen Notifications",
                NotificationManager.IMPORTANCE_HIGH
            )
            manager.createNotificationChannel(channel)
        }

        manager.notify(System.currentTimeMillis().toInt(), notification)
    }

    private fun sendTokenToServer(fcmToken: String) {
        // call your API to save the token
        // you can use your existing Retrofit here

        val sharedPref = getSharedPreferences("Auth", Context.MODE_PRIVATE)
        val authToken = "Bearer " + sharedPref.getString("token", null) ?: return

        val api = RetrofitInstance.api  // 👈 your existing Retrofit instance

        CoroutineScope(Dispatchers.IO).launch {
            try {
                api.updateFcmToken(authToken, FcmTokenRequest(fcmToken))
            } catch (e: Exception) {
                Log.e("FCM", "Failed to send token: ${e.message}")
            }
        }
    }
}