package com.example.seen.ui.community.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.seen.R
import com.example.seen.databinding.ItemCommunityPostBinding
import com.example.seen.domain.model.community.Data
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class PostAdapter(
    val context: Context
) : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    inner class PostViewHolder(val binding: ItemCommunityPostBinding) : RecyclerView.ViewHolder(binding.root)

    private val differCallback = object : DiffUtil.ItemCallback<Data>(){
        override fun areItemsTheSame(
            oldItem: Data,
            newItem: Data
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: Data,
            newItem: Data
        ): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, differCallback)


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PostViewHolder {
        val binding = ItemCommunityPostBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PostViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: PostViewHolder,
        position: Int
    ) {
        val post = differ.currentList[position]
        val (bgRes, colorRes, categoryRes) = setCategoryBackground(post.category ?: "")

        holder.binding.apply {
            tvUserPostName.text = post.user.full_name
            tvPostTime.text = getRelativeTime(post.created_at)
            tvCategory.text = context.getString(categoryRes)
            tvPostTitle.text = post.title
            tvPostContent.text = post.content
            flAvatarStroke.background = context.getDrawable(setProfileBackground(post.user.diabetes_type ?: ""))
            if(post.images.isNotEmpty()){
                Glide.with(root)
                    .load(post.images[0].url)
                    .into(ivPostImage)
            }
            tvCategory.background = context.getDrawable(bgRes)
            tvCategory.setTextColor(colorRes)
            tvLikesCount.text = post.likes_count.toString()
            tvCommentsCount.text = post.comments_count.toString()
            Glide.with(root)
                .load(post.user.profile_picture ?: "")
                .placeholder(R.drawable.ic_profile)
                .into(ivProfile)
            root.setOnClickListener {
                onItemClickListener?.invoke(post)
            }
        }
    }

    private var onItemClickListener: ((Data) -> Unit)? = null

    fun setOnItemClickListener(listener: (Data) -> Unit){
        onItemClickListener = listener
    }

    private fun setProfileBackground(message_type : String) = when (message_type) {
        "Type1" -> R.drawable.avatar_border_type1
        "Type2" -> R.drawable.avatar_border_type2
        "LADA" -> R.drawable.avatar_border_lada
        "MODY" -> R.drawable.avatar_border_mody
        else ->  R.drawable.avatar_border_gestational
    }

    private fun setCategoryBackground(message_type: String): Triple<Int, Int, Int> = when (message_type) {
        "Type1 / LADA"  -> Triple(R.drawable.bg_diabetes_type1,       R.color.profile_type1_stroke,       R.string.category_type1_lada)
        "Type2"         -> Triple(R.drawable.bg_diabetes_type2,        R.color.profile_type2_stroke,       R.string.category_type2)
        "MODY"          -> Triple(R.drawable.bg_diabetes_mody,         R.color.profile_mody_stroke,        R.string.category_mody)
        else            -> Triple(R.drawable.bg_diabetes_gestational,  R.color.profile_gestational_stroke, R.string.category_gestational)
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