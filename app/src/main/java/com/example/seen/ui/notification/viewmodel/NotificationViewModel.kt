package com.example.seen.ui.notification.viewmodel

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.seen.datasource.repository.CommunityRepository
import com.example.seen.datasource.repository.NotificationRepository
import com.example.seen.datasource.repository.ProfileRepository
import com.example.seen.domain.model.community.response.PostResponse
import com.example.seen.domain.model.notification.NotificationsResponse
import com.example.seen.util.Resource
import com.example.seen.util.SeenApplication
import kotlinx.coroutines.launch
import java.io.IOException

class NotificationViewModel(
    app: Application,
    private val notificationRepository: NotificationRepository,
    private val profileRepository: ProfileRepository,
    private val communityRepository: CommunityRepository
) : AndroidViewModel(app) {

    val notifications = MutableLiveData<Resource<NotificationsResponse>>()
    val markReadResult = MutableLiveData<Resource<Unit>?>()

    var notificationsPage = 1
    var notificationsResponse: NotificationsResponse? = null
    var notificationsIsLastPage = false

    val postResult = MutableLiveData<Resource<PostResponse>?>()
    val friendRequestResult = MutableLiveData<Resource<Unit>?>()

    fun getNotifications(token: String, isRefresh: Boolean = false) = viewModelScope.launch {
        if (isRefresh) {
            notificationsPage = 1
            notificationsResponse = null
            notificationsIsLastPage = false
        }
        safeGetNotificationsCall(token)
    }

    fun markAsRead(token: String, id: Int) = viewModelScope.launch {
        safeMarkAsReadCall(token, id)
    }

    fun markAllAsRead(token: String) = viewModelScope.launch {
        safeMarkAllAsReadCall(token)
    }

    fun clearMarkReadState() { markReadResult.value = null }

    fun deleteNotification(token: String, id: Int) = viewModelScope.launch {
        try {
            if (hasInternetConnection()) {
                notificationRepository.deleteNotification(token, id)
            }
        } catch (t: Throwable) { /* silent */ }
    }

    private suspend fun safeGetNotificationsCall(token: String) {
        notifications.postValue(Resource.Loading())
        try {
            if (hasInternetConnection()) {
                val response = notificationRepository.getNotifications(token, notificationsPage)
                if (response.isSuccessful) {
                    response.body()?.let { result ->
                        notificationsPage++
                        notificationsIsLastPage = result.meta.current_page >= result.meta.last_page

                        if (notificationsResponse == null) {
                            notificationsResponse = result
                        } else {
                            val merged = (notificationsResponse!!.notifications + result.notifications)
                            notificationsResponse = result.copy(notifications = merged)
                        }
                        notifications.postValue(Resource.Success(notificationsResponse!!))
                    }
                } else {
                    notifications.postValue(Resource.Error(response.message()))
                }
            } else {
                notifications.postValue(Resource.Error("No internet connection"))
            }
        } catch (t: Throwable) {
            when (t) {
                is IOException -> notifications.postValue(Resource.Error("Network Failure"))
                else -> notifications.postValue(Resource.Error("Conversion Error: ${t.message}"))
            }
        }
    }

    private suspend fun safeMarkAsReadCall(token: String, id: Int) {
        try {
            if (hasInternetConnection()) {
                notificationRepository.markAsRead(token, id)
            }
        } catch (t: Throwable) { /* silent — best effort */ }
    }

    private suspend fun safeMarkAllAsReadCall(token: String) {
        markReadResult.postValue(Resource.Loading())
        try {
            if (hasInternetConnection()) {
                val response = notificationRepository.markAllAsRead(token)
                if (response.isSuccessful) {
                    markReadResult.postValue(Resource.Success(Unit))
                } else {
                    markReadResult.postValue(Resource.Error(response.message()))
                }
            } else {
                markReadResult.postValue(Resource.Error("No internet connection"))
            }
        } catch (t: Throwable) {
            when (t) {
                is IOException -> markReadResult.postValue(Resource.Error("Network Failure"))
                else -> markReadResult.postValue(Resource.Error("Conversion Error: ${t.message}"))
            }
        }
    }

    fun getPost(token: String, postId: Int) = viewModelScope.launch {
        postResult.postValue(Resource.Loading())
        try {
            if (hasInternetConnection()) {
                val response = communityRepository.getPost(token, postId)
                if (response.isSuccessful) {
                    response.body()?.let {
                        postResult.postValue(Resource.Success(it))
                    }
                } else {
                    postResult.postValue(Resource.Error(response.message()))
                }
            } else {
                postResult.postValue(Resource.Error("No internet connection"))
            }
        } catch (t: Throwable) {
            when (t) {
                is IOException -> postResult.postValue(Resource.Error("Network Failure"))
                else -> postResult.postValue(Resource.Error("Conversion Error: ${t.message}"))
            }
        }
    }

    fun clearPostResult() { postResult.value = null }

    fun acceptFriendRequest(token: String, userId: Int) = viewModelScope.launch {
        safeFriendActionCall { profileRepository.acceptFriendRequest(token, userId) }
    }

    private suspend fun safeFriendActionCall(action: suspend () -> Unit) {
        friendRequestResult.postValue(Resource.Loading())
        try {
            if (hasInternetConnection()) {
                action()
                friendRequestResult.postValue(Resource.Success(Unit))
            } else {
                friendRequestResult.postValue(Resource.Error("No internet connection"))
            }
        } catch (t: Throwable) {
            when (t) {
                is IOException -> friendRequestResult.postValue(Resource.Error("Network Failure"))
                else -> friendRequestResult.postValue(Resource.Error("Conversion Error: ${t.message}"))
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