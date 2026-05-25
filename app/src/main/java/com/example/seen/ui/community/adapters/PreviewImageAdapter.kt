package com.example.seen.ui.community.adapters

import android.content.Context
import android.net.Uri
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
import com.example.seen.databinding.ItemPreviewImageBinding
import com.example.seen.domain.model.community.Comment
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

class PreviewImageAdapter(
    private val context: Context
) : RecyclerView.Adapter<PreviewImageAdapter.ImageViewHolder>() {

    inner class ImageViewHolder(
        val binding: ItemPreviewImageBinding
    ) : RecyclerView.ViewHolder(binding.root)

    private val differCallback = object : DiffUtil.ItemCallback<Uri>() {

        override fun areItemsTheSame(
            oldItem: Uri,
            newItem: Uri
        ): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(
            oldItem: Uri,
            newItem: Uri
        ): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, differCallback)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ImageViewHolder {

        val binding = ItemPreviewImageBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return ImageViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: ImageViewHolder,
        position: Int
    ) {

        val image = differ.currentList[position]

        holder.binding.apply {

            Glide.with(root)
                .load(image)
                .into(imageView)
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }
}