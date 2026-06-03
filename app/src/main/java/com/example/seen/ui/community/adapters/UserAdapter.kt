package com.example.seen.ui.community.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.seen.R
import com.example.seen.databinding.ItemAccountSearchBinding
import com.example.seen.databinding.ItemCommunityCommentBinding
import com.example.seen.domain.model.community.Comment
import com.example.seen.domain.model.community.PostUser
import com.example.seen.domain.model.entites.User

class UserAdapter : RecyclerView.Adapter<UserAdapter.UserViewHolder>(){

    inner class UserViewHolder(val binding: ItemAccountSearchBinding) : RecyclerView.ViewHolder(binding.root)

    private val differCallback = object : DiffUtil.ItemCallback<PostUser>(){
        override fun areItemsTheSame(
            oldItem: PostUser,
            newItem: PostUser
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: PostUser,
            newItem: PostUser
        ): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, differCallback)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): UserViewHolder {
        val binding = ItemAccountSearchBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: UserViewHolder,
        position: Int
    ) {
        val account = differ.currentList[position]
        holder.binding.apply {
            Glide.with(root)
                .load(account?.profile_picture?.takeIf { it.isNotEmpty() })
                .placeholder(R.drawable.ic_profile)
                .into(ivAccount)
            tvUserAccounttName.text = "${account.first_name} ${account.last_name}"
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }
}