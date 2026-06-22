package com.example.seen.ui.chat.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.seen.databinding.ItemMessageImageReciverBinding
import com.example.seen.databinding.ItemMessageImageSenderBinding
import com.example.seen.databinding.ItemMessageReciverBinding
import com.example.seen.databinding.ItemMessageSenderBinding
import com.example.seen.domain.model.chat.ChatMessage
import java.text.SimpleDateFormat
import java.util.Locale

class ChatMessagesAdapter(
    private var currentUserId: Int
) : ListAdapter<ChatMessage, RecyclerView.ViewHolder>(DiffCallback()) {

    companion object {
        private const val VIEW_TYPE_SENDER_TEXT  = 0
        private const val VIEW_TYPE_RECEIVER_TEXT = 1
        private const val VIEW_TYPE_SENDER_IMAGE  = 2
        private const val VIEW_TYPE_RECEIVER_IMAGE = 3
    }

    override fun getItemViewType(position: Int): Int {
        val msg = getItem(position)
        val isMine = msg.sender_id == currentUserId
        return when {
            isMine && msg.image_url != null  -> VIEW_TYPE_SENDER_IMAGE
            !isMine && msg.image_url != null -> VIEW_TYPE_RECEIVER_IMAGE
            isMine                           -> VIEW_TYPE_SENDER_TEXT
            else                             -> VIEW_TYPE_RECEIVER_TEXT
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_SENDER_TEXT ->
                SenderTextVH(ItemMessageSenderBinding.inflate(inflater, parent, false))
            VIEW_TYPE_RECEIVER_TEXT ->
                ReceiverTextVH(ItemMessageReciverBinding.inflate(inflater, parent, false))
            VIEW_TYPE_SENDER_IMAGE ->
                SenderImageVH(ItemMessageImageSenderBinding.inflate(inflater, parent, false))
            else ->
                ReceiverImageVH(ItemMessageImageReciverBinding.inflate(inflater, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val msg = getItem(position)
        when (holder) {
            is SenderTextVH    -> holder.bind(msg)
            is ReceiverTextVH  -> holder.bind(msg)
            is SenderImageVH   -> holder.bind(msg)
            is ReceiverImageVH -> holder.bind(msg)
        }
    }

    // ── ViewHolders ──────────────────────────────────────────────

    inner class SenderTextVH(private val b: ItemMessageSenderBinding) :
        RecyclerView.ViewHolder(b.root) {
        fun bind(msg: ChatMessage) {
            b.tvMessage.text = msg.message
            b.tvDate.text    = formatTime(msg.created_at)
            loadAvatar(b.ivProfile, msg.sender?.profile_picture)
        }
    }

    inner class ReceiverTextVH(private val b: ItemMessageReciverBinding) :
        RecyclerView.ViewHolder(b.root) {
        fun bind(msg: ChatMessage) {
            b.tvMessage.text = msg.message
            b.tvDate.text    = formatTime(msg.created_at)
            loadAvatar(b.ivProfile, msg.sender?.profile_picture)
        }
    }

    inner class SenderImageVH(private val b: ItemMessageImageSenderBinding) :
        RecyclerView.ViewHolder(b.root) {
        fun bind(msg: ChatMessage) {
            b.tvDate.text = formatTime(msg.created_at)
            loadAvatar(b.ivProfile, msg.sender?.profile_picture)
            Glide.with(b.ivMessageImage)
                .load(msg.image_url)
                .centerCrop()
                .into(b.ivMessageImage)
        }
    }

    inner class ReceiverImageVH(private val b: ItemMessageImageReciverBinding) :
        RecyclerView.ViewHolder(b.root) {
        fun bind(msg: ChatMessage) {
            b.tvDate.text = formatTime(msg.created_at)
            loadAvatar(b.ivProfile, msg.sender?.profile_picture)
            Glide.with(b.ivMessageImage)
                .load(msg.image_url)
                .centerCrop()
                .into(b.ivMessageImage)
        }
    }

    fun updateCurrentUserId(id: Int) {
        currentUserId = id
        notifyDataSetChanged()
    }

    // ── Helpers ──────────────────────────────────────────────────

    private fun loadAvatar(view: com.google.android.material.imageview.ShapeableImageView, url: String?) {
        if (!url.isNullOrEmpty()) {
            Glide.with(view).load(url).circleCrop().into(view)
        }
    }

    private fun formatTime(raw: String): String {
        return try {
            val parser    = SimpleDateFormat("yyyy-MM-dd hh:mm:ss a", Locale.ENGLISH)
            val formatter = SimpleDateFormat("hh:mm a", Locale.ENGLISH)
            formatter.format(parser.parse(raw)!!)
        } catch (e: Exception) { raw }
    }

    class DiffCallback : DiffUtil.ItemCallback<ChatMessage>() {
        override fun areItemsTheSame(a: ChatMessage, b: ChatMessage) = a.id == b.id
        override fun areContentsTheSame(a: ChatMessage, b: ChatMessage) = a == b
    }
}