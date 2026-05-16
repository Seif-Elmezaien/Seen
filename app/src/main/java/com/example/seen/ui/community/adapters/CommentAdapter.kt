package com.example.seen.ui.community.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.seen.R
import com.example.seen.databinding.ItemCommunityCommentBinding
import com.example.seen.domain.model.community.response.Comment
import com.example.seen.util.Constants.Companion.HIGH_GLUCOSE_VALUE
import com.example.seen.util.Constants.Companion.LOW_GLUCOSE_VALUE
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class CommentAdapter(
    val context: Context
) : RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {

    inner class CommentViewHolder(val binding: ItemCommunityCommentBinding) : RecyclerView.ViewHolder(binding.root)
    private val differCallback = object : DiffUtil.ItemCallback<Comment>(){
        override fun areItemsTheSame(
            oldItem: Comment,
            newItem: Comment
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: Comment,
            newItem: Comment
        ): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, differCallback)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CommentViewHolder {
        val binding = ItemCommunityCommentBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CommentViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: CommentViewHolder,
        position: Int
    ) {
        val comment = differ.currentList[position]
        holder.binding.apply {
            tvUserCommentName.text = comment.user.full_name
            tvCommentTime.text = getRelativeTime(comment.created_at ?: "")
            tvCommentsLikesCount.text = comment.likes_count.toString()
            flCommentAvatarStroke.background = context.getDrawable(setProfileBackground(comment.user.diabetes_type ?: ""))
            Glide.with(root)
                .load(comment.user.profile_picture ?: "")
                .placeholder(R.drawable.ic_profile)
                .into(ivCommentProfile)
            tvCommunty1stcomment.text = comment.comment_text
            ivCommentsLike.isSelected = comment.is_liked ?: false
        }
    }

    private fun setProfileBackground(messageType : String) = when (messageType) {
        "Type1" -> R.drawable.avatar_border_type1
        "Type2" -> R.drawable.avatar_border_type2
        "LADA" -> R.drawable.avatar_border_lada
        "MODY" -> R.drawable.avatar_border_mody
         else ->  R.drawable.avatar_border_gestational
    }

    fun getRelativeTime(isoTimestamp: String): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone("UTC")

        val date = sdf.parse(isoTimestamp) ?: return ""
        val now = Date()
        val diffMillis = now.time - date.time

        val seconds = diffMillis / 1000
        val minutes = seconds / 60
        val hours   = minutes / 60

        val calNow = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        val calDate = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { time = date }

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
                displayFormat.timeZone = TimeZone.getTimeZone("UTC")
                displayFormat.format(date)
            }
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }



}