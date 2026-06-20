package com.example.seen.ui.community.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.seen.R
import com.example.seen.databinding.ItemAccountSearchBinding
import com.example.seen.domain.model.community.PostUser
import com.example.seen.util.Constants.Companion.LADA
import com.example.seen.util.Constants.Companion.MODY
import com.example.seen.util.Constants.Companion.TYPE_1
import com.example.seen.util.Constants.Companion.TYPE_2

class LikesAdapter(val context: Context) : RecyclerView.Adapter<LikesAdapter.LikesViewHolder>() {

    inner class LikesViewHolder(val binding: ItemAccountSearchBinding) :
        RecyclerView.ViewHolder(binding.root)

    private val differCallback = object : DiffUtil.ItemCallback<PostUser>() {
        override fun areItemsTheSame(oldItem: PostUser, newItem: PostUser) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: PostUser, newItem: PostUser) =
            oldItem == newItem
    }

    val differ = AsyncListDiffer(this, differCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LikesViewHolder {
        val binding = ItemAccountSearchBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return LikesViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LikesViewHolder, position: Int) {
        val user = differ.currentList[position]
        holder.binding.apply {
            tvUserAccountName.text = user.first_name + " " + user.last_name
            flAccountAvatarStroke.background = ContextCompat.getDrawable(
                context, setProfileBackground(user.diabetes_type ?: "")
            )
            Glide.with(context)
                .load(user.profile_picture.takeIf { it.isNotEmpty() })
                .placeholder(R.drawable.ic_profile)
                .into(ivProfile)

            clSearchResult.setOnClickListener {
                onSearchResultClickListener?.invoke(user)
            }
        }
    }

    private var onSearchResultClickListener: ((PostUser) -> Unit)? = null

    fun setOnSearchResultClickListener(listener: (PostUser) -> Unit) {
        onSearchResultClickListener = listener
    }

    private fun setProfileBackground(diabetesType: String) = when (diabetesType) {
        TYPE_1 -> R.drawable.avatar_border_type1
        TYPE_2 -> R.drawable.avatar_border_type2
        LADA   -> R.drawable.avatar_border_lada
        MODY   -> R.drawable.avatar_border_mody
        else   -> R.drawable.avatar_border_gestational
    }

    override fun getItemCount() = differ.currentList.size
}