package com.example.seen.datasource.repository

import com.example.seen.datasource.remote.RetrofitInstance

class NotificationRepository {
    suspend fun getNotifications(token: String) =
        RetrofitInstance.api.getNotifications(token)

    suspend fun markAsRead(token: String, id: Int) =
        RetrofitInstance.api.markAsRead(token, id)

    suspend fun markAllAsRead(token: String) =
        RetrofitInstance.api.markAllAsRead(token)
}