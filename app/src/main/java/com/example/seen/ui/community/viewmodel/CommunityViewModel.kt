package com.example.seen.ui.community.viewmodel

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.example.seen.datasource.repository.CommunityRepository
import com.example.seen.datasource.repository.LogRepository
import com.example.seen.datasource.repository.UserRepository
import com.example.seen.domain.model.community.PostResponse
import com.example.seen.domain.model.community.response.PostListResponse
import com.example.seen.domain.model.entites.FullLog
import com.example.seen.util.NetworkUtils.hasInternetConnection
import com.example.seen.util.Resource
import com.example.seen.util.SeenApplication
import kotlinx.coroutines.launch
import retrofit2.Response
import java.io.IOException
import java.util.Calendar

class CommunityViewModel(
    app: Application,
    private val userRepository: UserRepository,
    private val communityRepository: CommunityRepository
) : AndroidViewModel(app) {


    val communityPosts =
        MutableLiveData<Resource<PostListResponse>>()
    var communityPostsPage = 1
    var communityPostsResponse: PostListResponse? = null

    init {
        getCommunityPosts(1, "all")
    }

    fun getCommunityPosts(page: Int, category: String) = viewModelScope.launch {
        safePostsCall(page, category)
    }

    private suspend fun safePostsCall(page: Int, category: String) {
        communityPosts.postValue(Resource.Loading())

        try {
            if (hasInternetConnection()) {

                val response = communityRepository.getCommunityPosts(page, category)

                communityPosts.postValue(handlePostsResponse(response))

            } else {
                communityPosts.postValue(Resource.Error("No internet connection"))
            }

        } catch (t: Throwable) {

            when (t) {
                is IOException ->
                    communityPosts.postValue(Resource.Error("Network Failure"))

                else ->
                    communityPosts.postValue(Resource.Error("Conversion Error"))
            }
        }
    }
    fun hasInternetConnection(): Boolean {
        val connectivityManager = getApplication<SeenApplication>().getSystemService(
            Context.CONNECTIVITY_SERVICE
        ) as ConnectivityManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val activeNetwork = connectivityManager.activeNetwork ?: return false
            val capabilities =
                connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
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

    private fun handlePostsResponse(response: Response<PostListResponse>)
            : Resource<PostListResponse> {

        if(response.isSuccessful){
            response.body()?.let { resultResponse ->

                communityPostsPage++

                if(communityPostsResponse == null){
                    communityPostsResponse = resultResponse
                } else {
                    val oldPosts = communityPostsResponse?.data
                    val newPosts = resultResponse.data
                    oldPosts?.addAll(newPosts)  // ✅ now works
                }

                return Resource.Success(
                    communityPostsResponse ?: resultResponse
                )
            }
        }

        return Resource.Error(response.message())
    }
}