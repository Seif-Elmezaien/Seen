package com.example.seen.ui.notification.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.seen.R
import com.example.seen.databinding.ItemNotificationBinding
import com.example.seen.domain.model.notification.NotificationItem
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

class NotificationAdapter(
    val context: Context
) : RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    inner class NotificationViewHolder(val binding: ItemNotificationBinding) :
        RecyclerView.ViewHolder(binding.root)

    private val differCallback = object : DiffUtil.ItemCallback<NotificationItem>() {
        override fun areItemsTheSame(oldItem: NotificationItem, newItem: NotificationItem) =
            oldItem.notification_id == newItem.notification_id
        override fun areContentsTheSame(oldItem: NotificationItem, newItem: NotificationItem) =
            oldItem == newItem
    }

    val differ = AsyncListDiffer(this, differCallback)

    private var onAcceptClickListener: ((NotificationItem) -> Unit)? = null
    private var onRejectClickListener: ((NotificationItem) -> Unit)? = null
    private var onDeleteClickListener: ((NotificationItem) -> Unit)? = null
    private var onItemClickListener: ((NotificationItem) -> Unit)? = null

    fun setOnAcceptClickListener(l: (NotificationItem) -> Unit) { onAcceptClickListener = l }
    fun setOnRejectClickListener(l: (NotificationItem) -> Unit) { onRejectClickListener = l }
    fun setOnDeleteClickListener(l: (NotificationItem) -> Unit) { onDeleteClickListener = l }
    fun setOnItemClickListener(l: (NotificationItem) -> Unit) { onItemClickListener = l }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val binding = ItemNotificationBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return NotificationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val item = differ.currentList[position]
        holder.binding.apply {

            tvNotificationDate.text = getRelativeTime(item.created_at!!)

            cvItemHomeLogs.strokeWidth = if (!item.is_read!!)
                2
            else
                0

            // icon + action buttons based on type
            when (item.type) {
                "friend_request" -> {
                    tvNotificationTitle.text = context.getString(R.string.notification_friend_request_title)
                    tvNotificationDescription.text = context.getString(R.string.notification_friend_request_description, item.extra_data?.username)
                    ivNotification.setImageResource(R.drawable.ic_add_friend_notification)
                    tvAcceptFriend.visibility = View.VISIBLE
                    tvRejectFriend.visibility = View.VISIBLE
                    tvDeleteNotification.visibility = View.GONE
                    tvAcceptFriend.setOnClickListener { onAcceptClickListener?.invoke(item) }
                    tvRejectFriend.setOnClickListener { onRejectClickListener?.invoke(item) }
                }
                "friend_accepted" -> {
                    tvNotificationTitle.text = context.getString(R.string.notification_accept_request_title)
                    tvNotificationDescription.text = context.getString(R.string.notification_accept_request_description, item.extra_data?.username)
                    ivNotification.setImageResource(R.drawable.ic_accept_friend_notification)
                    tvAcceptFriend.visibility = View.GONE
                    tvRejectFriend.visibility = View.GONE
                    tvDeleteNotification.visibility = View.VISIBLE
                    tvDeleteNotification.setOnClickListener { onDeleteClickListener?.invoke(item) }
                }
                "like" -> {
                    tvNotificationTitle.text = context.getString(R.string.notification_like_title)
                    tvNotificationDescription.text = context.getString(R.string.notification_like_description_single, item.extra_data?.username)
                    ivNotification.setImageResource(R.drawable.ic_commuinty_like_notification)
                    tvAcceptFriend.visibility = View.GONE
                    tvRejectFriend.visibility = View.GONE
                    tvDeleteNotification.visibility = View.VISIBLE
                    tvDeleteNotification.setOnClickListener { onDeleteClickListener?.invoke(item) }
                }

                "comment" -> {
                    tvNotificationTitle.text = context.getString(R.string.notification_comment_title)
                    tvNotificationDescription.text = context.getString(R.string.notification_comment_description_single, item.extra_data?.username)
                    ivNotification.setImageResource(R.drawable.ic_comment_notfication)
                    tvAcceptFriend.visibility = View.GONE
                    tvRejectFriend.visibility = View.GONE
                    tvDeleteNotification.visibility = View.VISIBLE
                    tvDeleteNotification.setOnClickListener { onDeleteClickListener?.invoke(item) }
                }

                else -> {
                    tvNotificationTitle.text = context.getString(R.string.notification_system_title)
                    tvNotificationDescription.text = context.getString(R.string.notification_system_description)
                    ivNotification.setImageResource(R.drawable.ic_notification_small)
                    tvAcceptFriend.visibility = View.GONE
                    tvRejectFriend.visibility = View.GONE
                    tvDeleteNotification.visibility = View.VISIBLE
                    tvDeleteNotification.setOnClickListener { onDeleteClickListener?.invoke(item) }
                }
            }

            root.setOnClickListener { onItemClickListener?.invoke(item) }
        }
    }

    fun getRelativeTime(timestamp: String): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd hh:mm:ss a", Locale.ENGLISH)
        sdf.timeZone = TimeZone.getTimeZone("Africa/Cairo")

        val date = try { sdf.parse(timestamp) } catch (e: Exception) { return "" } ?: return ""

        val now = Calendar.getInstance(TimeZone.getTimeZone("Africa/Cairo"))
        val diffMillis = now.timeInMillis - date.time

        val seconds = diffMillis / 1000
        val minutes = seconds / 60
        val hours   = minutes / 60

        val calNow = Calendar.getInstance(TimeZone.getTimeZone("Africa/Cairo"))
        val calDate = Calendar.getInstance(TimeZone.getTimeZone("Africa/Cairo")).apply { time = date }

        calNow.set(Calendar.HOUR_OF_DAY, 0); calNow.set(Calendar.MINUTE, 0)
        calNow.set(Calendar.SECOND, 0);      calNow.set(Calendar.MILLISECOND, 0)

        calDate.set(Calendar.HOUR_OF_DAY, 0); calDate.set(Calendar.MINUTE, 0)
        calDate.set(Calendar.SECOND, 0);       calDate.set(Calendar.MILLISECOND, 0)

        val calendarDays = ((calNow.timeInMillis - calDate.timeInMillis) / (1000 * 60 * 60 * 24)).toInt()

        return when {
            seconds < 60      -> if (seconds <= 1) context.getString(R.string.time_just_now)
            else context.getString(R.string.time_seconds_ago, seconds)
            minutes < 60      -> if (minutes == 1L) context.getString(R.string.time_one_minute_ago)
            else context.getString(R.string.time_minutes_ago, minutes)
            hours < 24        -> if (hours == 1L) context.getString(R.string.time_one_hour_ago)
            else context.getString(R.string.time_hours_ago, hours)
            calendarDays == 1 -> context.getString(R.string.time_one_day_ago)
            calendarDays == 2 -> context.getString(R.string.time_two_days_ago)
            calendarDays < 7  -> context.getString(R.string.time_days_ago, calendarDays)
            else              -> {
                val displayFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
                displayFormat.timeZone = TimeZone.getTimeZone("Africa/Cairo")
                displayFormat.format(date)
            }
        }
    }


    override fun getItemCount() = differ.currentList.size
}