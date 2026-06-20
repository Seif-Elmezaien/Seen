package com.example.seen.domain.model.notification

data class NotificationItem(
    val notification_id: Int?,
    val title: String?,
    val message: String?,
    val extra_data: ExtraData?,
    val type: String?,
    val is_read: Boolean?,
    val read_at: String?,
    val created_at: String?,
    val reference_id: String?,
    val time_ago: String?,
    val sender_image: String?
)
