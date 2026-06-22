package com.example.seen.util

import com.pusher.client.Pusher
import com.pusher.client.PusherOptions
import com.pusher.client.util.HttpAuthorizer
import com.pusher.client.channel.Channel
import com.pusher.client.connection.ConnectionEventListener
import com.pusher.client.connection.ConnectionState
import com.pusher.client.connection.ConnectionStateChange
import org.json.JSONObject

object ReverbManager {

    private var pusher: Pusher? = null

    fun connect(token: String) {
        if (pusher != null) return

        val authorizer = HttpAuthorizer(
            "https://inquisitorial-elba-undistractedly.ngrok-free.dev/broadcasting/auth" // no /api
        ).apply {
            setHeaders(mapOf("Authorization" to token))
        }

        val options = PusherOptions().apply {
            setHost("inquisitorial-elba-undistractedly.ngrok-free.dev")
            setWsPort(80)
            setWssPort(443)
            setUseTLS(true)
            setAuthorizer(authorizer)
        }

        pusher = Pusher("nkikujtwb07u8rilcoaz", options)
        pusher?.connect(object : ConnectionEventListener {
            override fun onConnectionStateChange(change: ConnectionStateChange) {
                android.util.Log.d("Reverb", "State: ${change.previousState} → ${change.currentState}")
            }
            override fun onError(message: String, code: String?, e: Exception?) {
                android.util.Log.e("Reverb", "Error: $message")
            }
        }, ConnectionState.ALL)
    }

    fun subscribeToChat(
        conversationId: Int,
        onMessageReceived: (JSONObject) -> Unit
    ) {
        try {
            val channel = pusher?.subscribePrivate("private-chat.$conversationId")
            channel?.bind(".MessageSent") { event ->
                android.util.Log.d("Reverb", "Event received: ${event.data}")
                try {
                    val data = JSONObject(event.data)
                    onMessageReceived(data)
                } catch (e: Exception) {
                    android.util.Log.e("Reverb", "Parse error: ${e.message}")
                }
            }
        } catch (e: IllegalArgumentException) {
            android.util.Log.d("Reverb", "Already subscribed to private-chat.$conversationId")
        }
    }

    fun unsubscribe(conversationId: Int) {
        pusher?.unsubscribe("private-chat.$conversationId")
    }
}