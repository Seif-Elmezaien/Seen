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
import com.example.seen.domain.model.community.PostUser
import com.example.seen.domain.model.community.request.CommentRequest
import com.example.seen.domain.model.community.response.AddCommentResponse
import com.example.seen.domain.model.community.response.CommentResponse
import com.example.seen.domain.model.community.response.PostListResponse
import com.example.seen.domain.model.community.response.SearchResponse
import com.example.seen.util.Resource
import com.example.seen.util.SeenApplication
import kotlinx.coroutines.launch
import retrofit2.Response
import java.io.IOException
import java.util.Collections.emptyList

class CommunityViewModel(
    app: Application,
    private val userRepository: UserRepository,
    private val communityRepository: CommunityRepository
) : AndroidViewModel(app) {


    val communityPosts =
        MutableLiveData<Resource<PostListResponse>>()
    var communityPostsPage = 1
    var communityPostsResponse: PostListResponse? = null

    val likePostResult = MutableLiveData<Resource<Unit>>()
    val likeError = MutableLiveData<Int>() // postId to revert

    val communityComment = MutableLiveData<Resource<CommentResponse>>()
    var communityCommentPage = 1
    var communityCommentResponse: CommentResponse? = null

    val addCommentResult = MutableLiveData<Resource<AddCommentResponse>?>()

    val editCommentResult = MutableLiveData<Resource<AddCommentResponse>?>()

    val deleteCommentResult = MutableLiveData<Resource<Unit>>()

    val likeCommentResult = MutableLiveData<Resource<Unit>>()

    val commentLikesResult = MutableLiveData<Resource<PostUser>>()


    val searchResults = MutableLiveData<Resource<SearchResponse>>()
    var searchPage = 1
    var searchResponse: SearchResponse? = null
    var lastSearchQuery: String = ""

    fun getCommunityPosts(token: String, category: String, isNewCategory: Boolean = false) = viewModelScope.launch {
        // If we are switching categories, reset the pagination state first
        if (isNewCategory) {
            communityPostsPage = 1
            communityPostsResponse = null
        }

        safePostsCall(token, category)
    }

    fun likePost(token: String, postId: Int) = viewModelScope.launch {
        safeLikePostCall(token, postId)
    }

    fun getPostComments(postId: Int, token: String) = viewModelScope.launch {
        safeCommentCall(postId, token)
    }

    fun addComment(token: String, postId: Int, content: String) = viewModelScope.launch {
        safeAddCommentCall(token, postId, content)
    }

    fun editComment(token: String, commentId: Int, content: String) = viewModelScope.launch {
        safeEditCommentCall(token, commentId, content)
    }

    fun deleteComment(token: String, commentId: Int) = viewModelScope.launch {
        safeDeleteCommentCall(token, commentId)
    }

    fun likeComment(token: String, commentId: Int) = viewModelScope.launch {
        safeLikeCommentCall(token, commentId)
    }

    fun getCommentLikes(token: String, commentId: Int) = viewModelScope.launch {
        safeGetCommentLikesCall(token, commentId)
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

    private fun handlePostsResponse(response: Response<PostListResponse>) : Resource<PostListResponse> {

        if(response.isSuccessful){
            response.body()?.let { resultResponse ->

                communityPostsPage++

                if(communityPostsResponse == null){
                    communityPostsResponse = resultResponse
                } else {
                    val newPosts = resultResponse.data
                    val mergedList = ((communityPostsResponse?.data ?: emptyList()) + newPosts).toMutableList()
                    communityPostsResponse = resultResponse.copy(data = mergedList)
                }

                return Resource.Success(
                    communityPostsResponse ?: resultResponse
                )
            }
        }

        return Resource.Error(response.message())
    }

    private suspend fun safeLikePostCall(token: String, postId: Int) {
        try {
            if (hasInternetConnection()) {
                val response = communityRepository.likePost(token, postId)
                if (response.isSuccessful) {
                    likePostResult.postValue(Resource.Success(Unit))
                } else {
                    likeError.postValue(postId) // revert UI
                }
            } else {
                likeError.postValue(postId)
            }
        } catch (t: Throwable) {
            likeError.postValue(postId)
        }
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
                    val newComment = resultResponse.comments
                    val mergedList = ((communityCommentResponse?.comments ?: emptyList()) + newComment).toMutableList()
                    communityCommentResponse = resultResponse.copy(comments = mergedList)
                }
                return Resource.Success(
                    communityCommentResponse ?: resultResponse
                )
            }
    }
    return Resource.Error(response.message())
    }

    private suspend fun safeAddCommentCall(token: String, postId: Int, content: String) {
        addCommentResult.postValue(Resource.Loading())
        try {
            if (hasInternetConnection()) {
                val response = communityRepository.addComment(token, postId, CommentRequest(content))
                if (response.isSuccessful) {
                    response.body()?.let {
                        addCommentResult.postValue(Resource.Success(it))
                    }
                } else {
                    addCommentResult.postValue(Resource.Error(response.message()))
                }
            } else {
                addCommentResult.postValue(Resource.Error("No internet connection"))
            }
        } catch (t: Throwable) {
            when (t) {
                is IOException -> addCommentResult.postValue(Resource.Error("Network Failure"))
                else -> addCommentResult.postValue(Resource.Error("Conversion Error: ${t.message}"))
            }
        }
    }

    fun clearAddCommentState() {
        addCommentResult.value = null
    }

    private suspend fun safeEditCommentCall(token: String, commentId: Int, content: String) {
        editCommentResult.postValue(Resource.Loading())
        try {
            if (hasInternetConnection()) {
                val response = communityRepository.editComment(token, commentId,
                    CommentRequest(content)
                )
                if (response.isSuccessful) {
                    response.body()?.let {
                        editCommentResult.postValue(Resource.Success(it))
                    }
                } else {
                    editCommentResult.postValue(Resource.Error(response.message()))
                }
            } else {
                editCommentResult.postValue(Resource.Error("No internet connection"))
            }
        } catch (t: Throwable) {
            when (t) {
                is IOException -> editCommentResult.postValue(Resource.Error("Network Failure"))
                else -> editCommentResult.postValue(Resource.Error("Conversion Error: ${t.message}"))
            }
        }
    }

    fun clearEditCommentState() {
        editCommentResult.value = null
    }


    private suspend fun safeDeleteCommentCall(token: String, commentId: Int) {
        deleteCommentResult.postValue(Resource.Loading())
        try {
            if (hasInternetConnection()) {
                val response = communityRepository.deleteComment(token, commentId)
                if (response.isSuccessful) {
                    deleteCommentResult.postValue(Resource.Success(Unit))
                } else {
                    deleteCommentResult.postValue(Resource.Error(response.message()))
                }
            } else {
                deleteCommentResult.postValue(Resource.Error("No internet connection"))
            }
        } catch (t: Throwable) {
            when (t) {
                is IOException -> deleteCommentResult.postValue(Resource.Error("Network Failure"))
                else -> deleteCommentResult.postValue(Resource.Error("Conversion Error: ${t.message}"))
            }
        }
    }

    private suspend fun safeLikeCommentCall(token: String, commentId: Int) {
        likeCommentResult.postValue(Resource.Loading())
        try {
            if (hasInternetConnection()) {
                val response = communityRepository.likeComment(token, commentId)
                if (response.isSuccessful) {
                    likeCommentResult.postValue(Resource.Success(Unit))
                } else {
                    likeCommentResult.postValue(Resource.Error(response.message()))
                }
            } else {
                likeCommentResult.postValue(Resource.Error("No internet connection"))
            }
        } catch (t: Throwable) {
            when (t) {
                is IOException -> likeCommentResult.postValue(Resource.Error("Network Failure"))
                else -> likeCommentResult.postValue(Resource.Error("Conversion Error: ${t.message}"))
            }
        }
    }

    private suspend fun safeGetCommentLikesCall(token: String, commentId: Int) {
        commentLikesResult.postValue(Resource.Loading())
        try {
            if (hasInternetConnection()) {
                val response = communityRepository.getCommentLikes(token, commentId)
                if (response.isSuccessful) {
                    response.body()?.let {
                        commentLikesResult.postValue(Resource.Success(it))
                    }
                } else {
                    commentLikesResult.postValue(Resource.Error(response.message()))
                }
            } else{
                commentLikesResult.postValue(Resource.Error("No internet connection"))
            }
        } catch (t: Throwable) {
            when (t) {
                is IOException -> commentLikesResult.postValue(Resource.Error("Network Failure"))
                else -> commentLikesResult.postValue(Resource.Error("Conversion Error: ${t.message}"))
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




}