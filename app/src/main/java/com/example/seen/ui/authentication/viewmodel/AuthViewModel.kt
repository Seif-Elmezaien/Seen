package com.example.seen.ui.authentication.viewmodel

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.ConnectivityManager.TYPE_ETHERNET
import android.net.ConnectivityManager.TYPE_MOBILE
import android.net.ConnectivityManager.TYPE_WIFI
import android.net.NetworkCapabilities.TRANSPORT_CELLULAR
import android.net.NetworkCapabilities.TRANSPORT_ETHERNET
import android.net.NetworkCapabilities.TRANSPORT_WIFI
import android.os.Build
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.application
import androidx.lifecycle.viewModelScope
import com.example.seen.R
import com.example.seen.SeenApplication
import com.example.seen.datasource.repository.AuthRepository
import com.example.seen.domain.model.User
import com.example.seen.domain.model.authentication.CheckEmailRequest
import com.example.seen.domain.model.authentication.CheckEmailResponse
import com.example.seen.domain.model.authentication.LoginAndSignupResponse
import com.example.seen.domain.model.authentication.LoginRequest
import com.example.seen.domain.model.authentication.SignupRequest
import com.example.seen.util.NetworkUtils
import com.example.seen.util.Resource
import kotlinx.coroutines.launch
import okio.IOException
import retrofit2.Response

class AuthViewModel(
    app: Application,
    private val authRepository: AuthRepository
) : AndroidViewModel(app) {

    private val _loginState = MutableLiveData<Resource<LoginAndSignupResponse>?>()
    val loginState: LiveData<Resource<LoginAndSignupResponse>?> = _loginState

    private val _emailExistState = MutableLiveData<Resource<CheckEmailResponse>?>()
    val emailExistState: LiveData<Resource<CheckEmailResponse>?> = _emailExistState

    private val _signupState = MutableLiveData<Resource<LoginAndSignupResponse>?>()
    val signupState: LiveData<Resource<LoginAndSignupResponse>?> = _signupState

    var signUpData = SignupRequest()


    fun login(loginRequest: LoginRequest) = viewModelScope.launch {
        _loginState.postValue(Resource.Loading())
        _loginState.postValue(safeApiCall { authRepository.login(loginRequest) })
    }

    fun checkEmailExist(email: CheckEmailRequest) = viewModelScope.launch {
        _emailExistState.postValue(Resource.Loading())
        _emailExistState.postValue(safeApiCall { authRepository.checkEmailExist(email) })
    }

    fun signup(user: SignupRequest) = viewModelScope.launch {
        _signupState.postValue(Resource.Loading())
        _signupState.postValue(safeApiCall { authRepository.signup(user) })
    }

    fun resetLoginState() {
        _loginState.postValue(null)
    }
    fun resetEmailExistState() {
        _emailExistState.postValue(null)
    }
    fun resetSignupState() {
        _signupState.postValue(null)
    }

    private suspend fun <T> safeApiCall(
        call: suspend () -> Response<T>
    ): Resource<T> {
        return try {
            if (NetworkUtils.hasInternetConnection(getApplication())) {
                val response = call()
                if (response.isSuccessful) {
                    Log.d("AuthViewModel", "safeApiCall: ${response.body()}")
                    response.body()?.let { return Resource.Success(it) }
                }
                Log.d("AuthViewModel", "safeApiCall: ${response.message()}")
                Resource.Error(response.message())
            } else {
                Resource.Error(getStringFromR(R.string.error_internet_connection))
            }
        } catch (t: Throwable) {
            Log.d("AuthViewModel", "safeApiCall: ${t.message}")
            when (t) {
                is IOException -> Resource.Error(getStringFromR(R.string.error_io_dispatcher))
                else -> Resource.Error(getStringFromR(R.string.error_conversion))
            }
        }
    }

    private fun getStringFromR(id: Int) =
        getApplication< SeenApplication>().getString(id)
}