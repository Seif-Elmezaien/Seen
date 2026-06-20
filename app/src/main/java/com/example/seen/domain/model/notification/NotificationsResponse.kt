package com.example.seen.domain.model.notification

import com.example.seen.domain.model.community.Meta

data class NotificationsResponse(
    val success: Boolean,
    val unread_count: Int,
    val notifications: List<NotificationItem>,
    val meta: Meta
)
