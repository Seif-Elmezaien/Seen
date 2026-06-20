package com.example.seen.ui.community.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.seen.R
import com.example.seen.databinding.ItemCommunityCommentBinding
import com.example.seen.domain.model.community.Comment
import com.example.seen.domain.model.community.PostUser
import com.example.seen.domain.model.community.response.AddCommentResponse
import com.example.seen.util.Constants.Companion.HIGH_GLUCOSE_VALUE
import com.example.seen.util.Constants.Companion.LADA
import com.example.seen.util.Constants.Companion.LOW_GLUCOSE_VALUE
import com.example.seen.util.Constants.Companion.MODY
import com.example.seen.util.Constants.Companion.TYPE_1
import com.example.seen.util.Constants.Companion.TYPE_2
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class CommentAdapter(
    val context: Context,
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

    var userId = -1
        set(value) {
            field = value
            notifyDataSetChanged() // rebinds all views with the correct userId
        }

    override fun onBindViewHolder(
        holder: CommentViewHolder,
        position: Int
    ) {
        val comment = differ.currentList[position]
        holder.binding.apply {
            tvUserCommentName.text = comment.user?.full_name ?: ""
            tvCommentTime.text = getRelativeTime(comment.created_at ?: "")
            flCommentAvatarStroke.background = ContextCompat.getDrawable(context, setProfileBackground(comment.user?.diabetes_type ?: ""))
            Glide.with(root)
                .load(comment.user?.profile_picture?.takeIf { it.isNotEmpty() })
                .placeholder(R.drawable.ic_profile)
                .into(ivProfile)
            tvCommentText.text = comment.comment_text
            ivCommentsLike.isSelected = comment.is_liked ?: false
            tvCommentsLikesCount.text = comment.likes_count.toString()

            ivCommentsLike.setOnClickListener {
                val currentList = differ.currentList.toMutableList()

                val adapterPosition = holder.bindingAdapterPosition
                if (adapterPosition == RecyclerView.NO_POSITION) return@setOnClickListener

                val currentComment = currentList[adapterPosition]

                val newLikedState = !(currentComment.is_liked ?: false)

                val updatedComment = currentComment.copy(
                    is_liked = newLikedState,
                    likes_count = currentComment.likes_count!! + if (newLikedState) 1 else -1
                )

                currentList[adapterPosition] = updatedComment
                differ.submitList(currentList)
                onLikeClickListener?.invoke(comment)
            }

            if (userId == comment.user?.id){
                Log.d("CommentAdapter", "Comment ID: ${comment.user.id} and userId: $userId")
                editComment.visibility = View.VISIBLE
                deleteComment.visibility = View.VISIBLE
            } else {
                Log.d("CommentAdapter", "Comment ID: ${comment.user?.id} and userId: $userId")
                editComment.visibility = View.GONE
                deleteComment.visibility = View.GONE
            }

            editComment.setOnClickListener    { onEditClickListener?.invoke(comment) }
            deleteComment.setOnClickListener  { onDeleteClickListener?.invoke(comment) }

            tvCommentsLikesCount.setOnClickListener {
                onLikeCountClickListener?.invoke(comment)
            }

            ivProfile.setOnClickListener {
                onProfileClickListener?.invoke(comment.user!!)
            }
        }
    }

    private var onLikeClickListener: ((Comment) -> Unit)? = null
    private var onEditClickListener: ((Comment) -> Unit)? = null
    private var onDeleteClickListener: ((Comment) -> Unit)? = null
    private var onProfileClickListener: ((PostUser) -> Unit)? = null


    fun setOnLikeClickListener(listener: (Comment) -> Unit) {
        onLikeClickListener = listener
    }
    fun setOnEditClickListener(listener: (Comment) -> Unit) {
        onEditClickListener = listener
    }
    fun setOnDeleteClickListener(listener: (Comment) -> Unit) {
        onDeleteClickListener = listener
    }
    fun setOnProfileClickListener(listener: (PostUser) -> Unit) {
        onProfileClickListener = listener
    }

    private var onLikeCountClickListener: ((Comment) -> Unit)? = null

    fun setOnLikeCountClickListener(listener: (Comment) -> Unit) {
        onLikeCountClickListener = listener
    }

    // ─── New: Helper to update one comment locally after like/edit ───
    fun addComment(comment: Comment, onInserted: () -> Unit) {
        val newList = differ.currentList.toMutableList()
        newList.add(0, comment)
        differ.submitList(newList.toList()) {
            onInserted() // called after diff is done and list is applied
        }
    }

    fun updateComment(comment: Comment) {
        val newList = differ.currentList.toMutableList()
        val index = newList.indexOfFirst { it.id == comment.id }
        if (index != -1) {
            newList[index] = comment
            differ.submitList(newList.toList())
        }
    }

    fun removeComment(commentId: Int) {
        val newList = differ.currentList.toMutableList()
        newList.removeAll { it.id == commentId }
        differ.submitList(newList.toList())
    }

    private fun setProfileBackground(messageType : String) = when (messageType) {
        TYPE_1 -> R.drawable.avatar_border_type1
        TYPE_2 -> R.drawable.avatar_border_type2
        LADA -> R.drawable.avatar_border_lada
        MODY -> R.drawable.avatar_border_mody
        else ->  R.drawable.avatar_border_gestational
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

    override fun getItemCount(): Int {
        return differ.currentList.size
    }
}