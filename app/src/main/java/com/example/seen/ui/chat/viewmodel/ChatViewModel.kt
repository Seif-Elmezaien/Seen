package com.example.seen.ui.chat.viewmodel

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.seen.datasource.repository.ChatRepository
import com.example.seen.datasource.repository.UserRepository
import com.example.seen.domain.model.chat.ChatMessage
import com.example.seen.domain.model.chat.SendMessageResponse
import com.example.seen.domain.model.chat.response.ConversationResponse
import com.example.seen.domain.model.chat.response.ConversationSearchResponse
import com.example.seen.util.Resource
import com.example.seen.util.SeenApplication
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import java.io.IOException

class ChatViewModel(
    app: Application,
    private val chatRepository: ChatRepository,
    private val userRepository: UserRepository
) : AndroidViewModel(app) {

    val conversations  = MutableLiveData<Resource<ConversationResponse>>()
    val searchResults  = MutableLiveData<Resource<ConversationSearchResponse>>()
    val messages       = MutableLiveData<Resource<List<ChatMessage>>>()
    val sendResult     = MutableLiveData<Resource<SendMessageResponse>>()

    // ── Conversations ─────────────────────────────────────────────

    fun getConversations(token: String) = viewModelScope.launch {
        safeGetConversationsCall(token)
    }

    fun searchChatFriends(token: String, query: String) = viewModelScope.launch {
        safeSearchChatFriendsCall(token, query)
    }

    // ── Messages ──────────────────────────────────────────────────

    fun getMessages(token: String, receiverId: Int) = viewModelScope.launch {
        safeGetMessagesCall(token, receiverId)
    }

    fun sendMessage(
        token: String,
        receiverId: Int,
        text: String?,
        imagePart: MultipartBody.Part? = null
    ) = viewModelScope.launch {
        safeSendMessageCall(token, receiverId, text, imagePart)
    }

    // ── Safe calls ────────────────────────────────────────────────

    private suspend fun safeGetConversationsCall(token: String) {
        conversations.postValue(Resource.Loading())
        try {
            if (hasInternetConnection()) {
                val response = chatRepository.getConversations(token)
                if (response.isSuccessful) {
                    response.body()?.let { conversations.postValue(Resource.Success(it)) }
                } else {
                    conversations.postValue(Resource.Error(response.message()))
                }
            } else {
                conversations.postValue(Resource.Error("No internet connection"))
            }
        } catch (t: Throwable) {
            when (t) {
                is IOException -> conversations.postValue(Resource.Error("Network Failure"))
                else -> conversations.postValue(Resource.Error("Conversion Error: ${t.message}"))
            }
        }
    }

    private suspend fun safeSearchChatFriendsCall(token: String, query: String) {
        searchResults.postValue(Resource.Loading())
        try {
            if (hasInternetConnection()) {
                val response = chatRepository.searchChatFriends(token, query)
                if (response.isSuccessful) {
                    response.body()?.let { searchResults.postValue(Resource.Success(it)) }
                } else {
                    searchResults.postValue(Resource.Error(response.message()))
                }
            } else {
                searchResults.postValue(Resource.Error("No internet connection"))
            }
        } catch (t: Throwable) {
            when (t) {
                is IOException -> searchResults.postValue(Resource.Error("Network Failure"))
                else -> searchResults.postValue(Resource.Error("Conversion Error: ${t.message}"))
            }
        }
    }

    private suspend fun safeGetMessagesCall(token: String, receiverId: Int) {
        messages.postValue(Resource.Loading())
        try {
            if (hasInternetConnection()) {
                val response = chatRepository.getMessages(token, receiverId)
                if (response.isSuccessful) {
                    val list = response.body()?.data ?: emptyList()
                    messages.postValue(Resource.Success(list))
                } else {
                    messages.postValue(Resource.Error(response.message()))
                }
            } else {
                messages.postValue(Resource.Error("No internet connection"))
            }
        } catch (t: Throwable) {
            when (t) {
                is IOException -> messages.postValue(Resource.Error("Network Failure"))
                else -> messages.postValue(Resource.Error("Conversion Error: ${t.message}"))
            }
        }
    }

    private suspend fun safeSendMessageCall(
        token: String,
        receiverId: Int,
        text: String?,
        imagePart: MultipartBody.Part?
    ) {
        sendResult.postValue(Resource.Loading())
        try {
            if (hasInternetConnection()) {
                val response = chatRepository.sendMessage(token, receiverId, text, imagePart)
                if (response.isSuccessful) {
                    response.body()?.let { sendResult.postValue(Resource.Success(it)) }
                } else {
                    sendResult.postValue(Resource.Error(response.message()))
                }
            } else {
                sendResult.postValue(Resource.Error("No internet connection"))
            }
        } catch (t: Throwable) {
            when (t) {
                is IOException -> sendResult.postValue(Resource.Error("Network Failure"))
                else -> sendResult.postValue(Resource.Error("Conversion Error: ${t.message}"))
            }
        }
    }

    fun getCurrentUser() = userRepository.getUser()

    private fun hasInternetConnection(): Boolean {
        val cm = getApplication<SeenApplication>()
            .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = cm.activeNetwork ?: return false
            val caps    = cm.getNetworkCapabilities(network) ?: return false
            return caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                    caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                    caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
        } else {
            @Suppress("DEPRECATION")
            cm.activeNetworkInfo?.run {
                return type == ConnectivityManager.TYPE_WIFI ||
                        type == ConnectivityManager.TYPE_MOBILE
            }
        }
        return false
    }
}