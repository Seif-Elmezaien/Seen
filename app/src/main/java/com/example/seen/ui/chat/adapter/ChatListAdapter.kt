package com.example.seen.ui.chat.adapter

import android.content.Context
import android.text.style.LeadingMarginSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.seen.R
import com.example.seen.databinding.ItemChatListBinding
import com.example.seen.domain.model.chat.Conversation
import com.example.seen.domain.model.chatbot.ChatMessage
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.Markwon
import io.noties.markwon.MarkwonSpansFactory
import org.commonmark.node.Paragraph
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

class ChatListAdapter(
    private val context: Context,
    private var currentUserId: Int
) : RecyclerView.Adapter<ChatListAdapter.ChatViewHolder>() {

    inner class ChatViewHolder(
        val binding: ItemChatListBinding
    ) : RecyclerView.ViewHolder(binding.root)

    private val differCallback = object : DiffUtil.ItemCallback<Conversation>() {

        override fun areItemsTheSame(
            oldItem: Conversation,
            newItem: Conversation
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: Conversation,
            newItem: Conversation
        ): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, differCallback)

    private var onItemClickListener: ((Conversation) -> Unit)? = null

    fun setOnItemClickListener(
        listener: (Conversation) -> Unit
    ) {
        onItemClickListener = listener
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ChatViewHolder {

        val binding = ItemChatListBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return ChatViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: ChatViewHolder,
        position: Int
    ) {

        val conversation = differ.currentList[position]

        holder.binding.apply {

            val otherUser =
                if (conversation.user1_id == currentUserId)
                    conversation.user2
                else
                    conversation.user1


            tvUsername.text =
                "${otherUser?.first_name ?: ""} ${otherUser?.last_name ?: ""}"

            tvLastMessage.text =
                conversation.latest_message?.message ?: ""

            tvNotificationDate.text =
                conversation.latest_message?.created_at?.let {
                    getRelativeTime(it)
                } ?: ""

            root.setOnClickListener {
                onItemClickListener?.invoke(conversation)
            }
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    fun updateCurrentUserId(id: Int) {
        currentUserId = id
        notifyDataSetChanged()
    }

    private fun getRelativeTime(timestamp: String): String {

        val sdf = SimpleDateFormat(
            "yyyy-MM-dd hh:mm:ss a",
            Locale.ENGLISH
        )

        sdf.timeZone = TimeZone.getTimeZone("Africa/Cairo")

        val date = try {
            sdf.parse(timestamp)
        } catch (e: Exception) {
            return ""
        } ?: return ""

        val now = Calendar.getInstance(
            TimeZone.getTimeZone("Africa/Cairo")
        )

        val diffMillis = now.timeInMillis - date.time

        val seconds = diffMillis / 1000
        val minutes = seconds / 60
        val hours = minutes / 60

        return when {
            seconds < 60 -> "now"
            minutes < 60 -> "${minutes}m"
            hours < 24 -> "${hours}h"
            else -> {
                val format = SimpleDateFormat(
                    "dd/MM",
                    Locale.getDefault()
                )
                format.format(date)
            }
        }
    }
}