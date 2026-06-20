package com.example.seen.datasource.repository

import com.example.seen.datasource.remote.RetrofitInstance

class NotificationRepository {
    suspend fun getNotifications(token: String, page: Int = 1) =
        RetrofitInstance.api.getNotifications(token, page)

    suspend fun markAsRead(token: String, id: Int) =
        RetrofitInstance.api.markAsRead(token, id)

    suspend fun markAllAsRead(token: String) =
        RetrofitInstance.api.markAllAsRead(token)

    suspend fun deleteNotification(token: String, id: Int) =
        RetrofitInstance.api.deleteNotification(token, id)
}