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
import com.example.seen.databinding.ItemCommunityPostBinding
import com.example.seen.domain.model.community.Data
import com.example.seen.ui.community.dialog.ImagePreviewDialogFragment
import com.example.seen.util.Constants.Companion.ADVICES
import com.example.seen.util.Constants.Companion.GESTATIONAL
import com.example.seen.util.Constants.Companion.LADA
import com.example.seen.util.Constants.Companion.MODY
import com.example.seen.util.Constants.Companion.TYPE1_LADA
import com.example.seen.util.Constants.Companion.TYPE_1
import com.example.seen.util.Constants.Companion.TYPE_2
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class PostAdapter(
    val context: Context,
    private val fragmentManager: androidx.fragment.app.FragmentManager  // 👈 add
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
            flAvatarStroke.background = ContextCompat.getDrawable(context, setProfileBackground(post.user.diabetes_type ?: ""))
            tvCategory.background = ContextCompat.getDrawable(context, bgRes)

            bindPhotos(holder.binding, post.images.map { it.url }, fragmentManager)

            tvCategory.background = ContextCompat.getDrawable(context, bgRes)
            tvCategory.setTextColor(ContextCompat.getColor(context, colorRes))
            ivLike.isSelected = post.is_liked ?: false
            tvLikesCount.text = post.likes_count.toString()
            tvCommentsCount.text = post.comments_count.toString()
            Glide.with(root)
                .load(post.user.profile_picture.takeIf { it.isNotEmpty() })
                .placeholder(R.drawable.ic_profile)
                .into(ivProfile)
            ivComment.setOnClickListener {
                onCommentClickListener?.invoke(post)
            }

            ivLike.setOnClickListener {

                val currentList = differ.currentList.toMutableList()

                val adapterPosition = holder.bindingAdapterPosition
                if (adapterPosition == RecyclerView.NO_POSITION) return@setOnClickListener

                val currentPost = currentList[adapterPosition]

                val newLikedState = !(currentPost.is_liked ?: false)

                val updatedPost = currentPost.copy(
                    is_liked = newLikedState,
                    likes_count = currentPost.likes_count + if (newLikedState) 1 else -1
                )

                currentList[adapterPosition] = updatedPost

                differ.submitList(currentList)

                onLikeClickListener?.invoke(updatedPost)
            }
        }
    }

    private var onCommentClickListener: ((Data) -> Unit)? = null
    private var onLikeClickListener: ((Data) -> Unit)? = null

    fun setOnCommentClickListener(listener: (Data) -> Unit){
        onCommentClickListener = listener
    }

    fun setOnLikeClickListener(listener: (Data) -> Unit){
        onLikeClickListener = listener
    }

    private fun setProfileBackground(messageType : String) = when (messageType) {
        TYPE_1 -> R.drawable.avatar_border_type1
        TYPE_2 -> R.drawable.avatar_border_type2
        LADA -> R.drawable.avatar_border_lada
        MODY -> R.drawable.avatar_border_mody
        else ->  R.drawable.avatar_border_gestational
    }

    private fun setCategoryBackground(messageType: String): Triple<Int, Int, Int> = when (messageType) {
        TYPE1_LADA      -> Triple(R.drawable.bg_diabetes_type1,        R.color.profile_type1_stroke,       R.string.category_type1_lada)
        TYPE_2          -> Triple(R.drawable.bg_diabetes_type2,        R.color.profile_type2_stroke,       R.string.category_type2)
        MODY            -> Triple(R.drawable.bg_diabetes_mody,         R.color.profile_mody_stroke,        R.string.category_mody)
        GESTATIONAL     -> Triple(R.drawable.bg_diabetes_gestational,  R.color.profile_gestational_stroke, R.string.category_gestational)
        ADVICES         -> Triple(R.drawable.bg_diabetes_advise,       R.color.advise_gray,                R.string.category_advise)
        else            -> Triple(R.drawable.bg_diabetes_general,      R.color.general_yellow,             R.string.category_general)
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

    private fun bindPhotos(binding: ItemCommunityPostBinding, photos: List<String>, fragment: androidx.fragment.app.FragmentManager) {
        binding.apply {
            when (photos.size) {
                0 -> {
                    ivPhoto1.visibility = View.GONE
                    layoutRow2.visibility = View.GONE
                }
                1 -> {
                    ivPhoto1.visibility = View.VISIBLE
                    layoutRow2.visibility = View.GONE
                    Glide.with(context).load(photos[0]).into(ivPhoto1)
                    ivPhoto1.setOnClickListener { openViewer(photos, 0, fragment) }
                }
                2 -> {
                    ivPhoto1.visibility = View.VISIBLE
                    layoutRow2.visibility = View.VISIBLE
                    flPhoto3.visibility = View.GONE
                    Glide.with(context).load(photos[0]).into(ivPhoto1)
                    Glide.with(context).load(photos[1]).into(ivPhoto2)
                    ivPhoto1.setOnClickListener { openViewer(photos, 0, fragment) }
                    ivPhoto2.setOnClickListener { openViewer(photos, 1, fragment) }
                }
                else -> {
                    ivPhoto1.visibility = View.VISIBLE
                    layoutRow2.visibility = View.VISIBLE
                    flPhoto3.visibility = View.VISIBLE
                    Glide.with(context).load(photos[0]).into(ivPhoto1)
                    Glide.with(context).load(photos[1]).into(ivPhoto2)
                    Glide.with(context).load(photos[2]).into(ivPhoto3)
                    ivPhoto1.setOnClickListener { openViewer(photos, 0, fragment) }
                    ivPhoto2.setOnClickListener { openViewer(photos, 1, fragment) }
                    ivPhoto3.setOnClickListener { openViewer(photos, 2, fragment) }

                    if (photos.size > 3) {
                        tvMoreCount.visibility = View.VISIBLE
                        tvMoreCount.text = "+${photos.size - 3}"
                    } else {
                        tvMoreCount.visibility = View.GONE
                    }
                }
            }
        }
    }

    private fun openViewer(photos: List<String>, startPosition: Int, fragmentManager: androidx.fragment.app.FragmentManager) {
        ImagePreviewDialogFragment(
            images = photos.toMutableList(),
            startPosition = startPosition,
            isDeletable = false      // 👈 no delete in feed
        ).show(fragmentManager, "image_viewer")
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

}