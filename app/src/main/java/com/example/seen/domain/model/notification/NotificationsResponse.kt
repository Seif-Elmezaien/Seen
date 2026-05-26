package com.example.seen.domain.model.notification

data class NotificationsResponse(
    val success: Boolean,
    val unread_count: Int,
    val notifications: List<NotificationItem>
)
