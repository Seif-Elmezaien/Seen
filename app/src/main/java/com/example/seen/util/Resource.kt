package com.example.seen.util

sealed class Resource <T>(
    val data: T? = null,
    val message : String? = null
) {
    //only this class can access on Resources
    class Success<T>(data: T) : Resource<T>(data)
    class Error<T>(message: String, data: T? = null) : Resource<T>(data, message)
    class Loading<T> : Resource<T>()
}