package com.example.seen.ui.notification.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.seen.R
import com.example.seen.databinding.ItemHomeLogsBinding
import com.example.seen.databinding.ItemNotificationBinding
import com.example.seen.domain.model.entites.FullLog
import com.example.seen.domain.model.entites.Reminder
import com.example.seen.domain.model.notification.NotificationItem
import com.example.seen.util.Constants.Companion.HIGH_GLUCOSE_VALUE
import com.example.seen.util.Constants.Companion.LOW_GLUCOSE_VALUE
import java.text.SimpleDateFormat
import java.util.Date

class NotificationAdapter(
    val context: Context
): RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    inner class NotificationViewHolder(val binding: ItemNotificationBinding) : RecyclerView.ViewHolder(binding.root)

    //we will use differ list because its more accurate : Because it compare between two lists in the background so it wont update the list every time
    private val differCallback = object : DiffUtil.ItemCallback<NotificationItem>(){

        override fun areItemsTheSame(
            oldItem: NotificationItem,
            newItem: NotificationItem)
                : Boolean {
            return oldItem.notification_id == newItem.notification_id
        }

        override fun areContentsTheSame(
            oldItem: NotificationItem,
            newItem: NotificationItem)
        : Boolean {
            return oldItem == newItem
        }
    }

    //AsyncListDiffer is a tool that takes two list and compare them and cal the differences
    val differ = AsyncListDiffer(this, differCallback)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): NotificationViewHolder {
        val binding = ItemNotificationBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false)
        return NotificationViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: NotificationViewHolder,
        position: Int
    ) {
        val notificationItem = differ.currentList[position]

        holder.binding.apply {
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

}