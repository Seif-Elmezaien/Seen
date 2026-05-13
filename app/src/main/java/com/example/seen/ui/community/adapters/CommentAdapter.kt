package com.example.seen.ui.community.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.seen.R
import com.example.seen.databinding.ItemCommunityCommentBinding
import com.example.seen.domain.model.community.response.Comment

class CommentAdapter : RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {

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
            tvCommentTime.text = comment.created_at
            tvCommentsLikesCount.text = comment.likes_count.toString()
            Glide.with(root)
                .load(toHttp(comment.user.profile_picture) ?: "")
                .placeholder(R.drawable.ic_profile)
                .into(ivCommentProfile)
            tvCommunty1stcomment.text = comment.comment_text

            ivCommentsLike.isSelected = comment.is_liked ?: false
        }

    }

    fun toHttp(url: String?): String {
        return url?.replaceFirst("https://", "http://") ?: ""
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }



}