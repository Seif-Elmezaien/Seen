package com.example.seen.ui.community.viewmodel

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.seen.datasource.repository.CommunityRepository
import com.example.seen.datasource.repository.UserRepository
import com.example.seen.domain.model.community.Comment
import com.example.seen.domain.model.community.response.CommentResponse
import com.example.seen.domain.model.community.response.PostListResponse
import com.example.seen.util.Resource
import com.example.seen.util.SeenApplication
import kotlinx.coroutines.launch
import retrofit2.Response
import java.io.IOException

class CommunityViewModel(
    app: Application,
    private val userRepository: UserRepository,
    private val communityRepository: CommunityRepository
) : AndroidViewModel(app) {


    val communityPosts =
        MutableLiveData<Resource<PostListResponse>>()
    var communityPostsPage = 1
    var communityPostsResponse: PostListResponse? = null

    val communityComment = MutableLiveData<Resource<CommentResponse>>()

    var communityCommentPage = 1

    var communityCommentResponse: CommentResponse? = null


    fun getCommunityPosts( token: String, category: String) = viewModelScope.launch {
        safePostsCall(token, category)
    }

    fun getPostComments(postId: Int, token: String) = viewModelScope.launch {
        safeCommentCall(postId, token)
    }

    private suspend fun safePostsCall(token: String, category: String) {
        communityPosts.postValue(Resource.Loading())

        try {
            if (hasInternetConnection()) {

                val response = communityRepository.getCommunityPosts(token, communityPostsPage, category)

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

    private fun handlePostsResponse(response: Response<PostListResponse>) : Resource<PostListResponse> {

        if(response.isSuccessful){
            response.body()?.let { resultResponse ->

                communityPostsPage++

                if(communityPostsResponse == null){
                    communityPostsResponse = resultResponse
                } else {
                    val oldPosts = communityPostsResponse?.data
                    val newPosts = resultResponse.data
                    oldPosts?.addAll(newPosts)
                }

                return Resource.Success(
                    communityPostsResponse ?: resultResponse
                )
            }
        }

        return Resource.Error(response.message())
    }

    private suspend fun safeCommentCall(id: Int, token: String){
        communityComment.postValue(Resource.Loading())

        try {
            if (hasInternetConnection()) {
                val response = communityRepository.getPostComments(token, id, communityCommentPage)
                communityComment.postValue(handleCommentResponse(response))
            } else {
                communityComment.postValue(Resource.Error("No internet connection"))

        }
            } catch (t: Throwable) {
            when (t) {
                is IOException ->
                    communityComment.postValue(Resource.Error("Network Failure"))
                else -> communityComment.postValue(Resource.Error("Conversion Error: ${t.message}"))
            }
    }
    }

    private fun handleCommentResponse(response: Response<CommentResponse>): Resource<CommentResponse> {
        if (response.isSuccessful) {
            response.body()?.let { resultResponse ->

                communityCommentPage++

                if (communityCommentResponse == null) {
                    communityCommentResponse = resultResponse
                } else {
                    val oldComments = communityCommentResponse?.comments
                    val newComments = resultResponse.comments
                    oldComments?.addAll(newComments)
                }
                return Resource.Success(
                    communityCommentResponse ?: resultResponse
                )
            }
    }
    return Resource.Error(response.message())
    }

}