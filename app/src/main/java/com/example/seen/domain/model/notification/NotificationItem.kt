package com.example.seen.domain.model.notification

data class NotificationItem(
    val notification_id: Int,
    val title: String,
    val body: String,
    val type: String,
    val is_read: Boolean,
    val read_at: String?,
    val created_at: String
)
