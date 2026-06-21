package com.example.seen.ui.chat.viewmodel

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.seen.R
import com.example.seen.datasource.repository.ChatRepository
import com.example.seen.datasource.repository.ChatbotRepository
import com.example.seen.domain.model.chat.response.ConversationResponse
import com.example.seen.domain.model.chat.response.ConversationSearchResponse
import com.example.seen.domain.model.chatbot.ChatMessage
import com.example.seen.util.Resource
import com.example.seen.util.SeenApplication
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.IOException

class ChatViewModel(
    app: Application,
    private val chatRepository: ChatRepository
) : AndroidViewModel(app) {

    val conversations = MutableLiveData<Resource<ConversationResponse>>()
    val searchResults = MutableLiveData<Resource<ConversationSearchResponse>>()

    fun getConversations(token: String) = viewModelScope.launch {
        safeGetConversationsCall(token)
    }

    fun searchChatFriends(token: String, query: String) = viewModelScope.launch {
        safeSearchChatFriendsCall(token, query)
    }

    private suspend fun safeGetConversationsCall(token: String) {
        conversations.postValue(Resource.Loading())

        try {
            if (hasInternetConnection()) {
                val response = chatRepository.getConversations(token)

                if (response.isSuccessful) {
                    response.body()?.let {
                        conversations.postValue(Resource.Success(it))
                    }
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

    private suspend fun safeSearchChatFriendsCall(
        token: String,
        query: String
    ) {
        searchResults.postValue(Resource.Loading())

        try {
            if (hasInternetConnection()) {
                val response = chatRepository.searchChatFriends(token, query)

                if (response.isSuccessful) {
                    response.body()?.let {
                        searchResults.postValue(Resource.Success(it))
                    }
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

    private fun hasInternetConnection(): Boolean {
        val connectivityManager = getApplication<SeenApplication>().getSystemService(
            Context.CONNECTIVITY_SERVICE
        ) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val activeNetwork = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
            return when {
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                else -> false
            }
        } else {
            connectivityManager.activeNetworkInfo?.run {
                return when (type) {
                    ConnectivityManager.TYPE_WIFI -> true
                    ConnectivityManager.TYPE_MOBILE -> true
                    else -> false
                }
            }
        }
        return false
    }
}