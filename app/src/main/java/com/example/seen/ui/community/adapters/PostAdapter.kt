package com.example.seen.ui.community.adapters

import android.app.FragmentContainer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.seen.databinding.ItemCommunityPostBinding
import com.example.seen.domain.model.community.Data

class PostAdapter : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

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
        holder.binding.apply {
            tvUserPostName.text = post.user.full_name
            tvPostTime.text = post.created_at
            tvCategory.text = post.category
            tvPostTitle.text = post.title
            tvPostContent.text = post.content
            if(post.images.isNotEmpty()){
                Glide.with(root)
                    .load(post.images[0].media)
                    .into(ivPostImage)
            }
            tvLikesCount.text = post.likes_count.toString()
            tvCommentsCount.text = post.comments_count.toString()
            if(post.user.profile_picture.isNotEmpty()){
                Glide.with(root)
                    .load(post.user.profile_picture)
                    .into(ivProfile)
            }
            root.setOnClickListener {
                onItemClickListener?.invoke(post)
            }
        }
    }

    private var onItemClickListener: ((Data) -> Unit)? = null

    fun setOnItemClickListener(listener: (Data) -> Unit){
        onItemClickListener = listener
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

}