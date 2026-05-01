package com.example.seen.ui.home.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.seen.databinding.ItemHomeLogsBinding
import com.example.seen.domain.model.entites.FullLog

class HomeAdapter: RecyclerView.Adapter<HomeAdapter.HomeViewHolder>() {

    inner class HomeViewHolder(val binding: ItemHomeLogsBinding) : RecyclerView.ViewHolder(binding.root)

    //we will use differ list because its more accurate : Because it compare between two lists in the background so it wont update the list every time
    private val differCallback = object : DiffUtil.ItemCallback<FullLog>(){

        override fun areItemsTheSame(
            oldItem: FullLog,
            newItem: FullLog)
                : Boolean {
            return oldItem.log.log_id == newItem.log.log_id
        }

        override fun areContentsTheSame(
            oldItem: FullLog,
            newItem: FullLog)
        : Boolean {
            return oldItem == newItem
        }
    }

    //AsyncListDiffer is a tool that takes two list and compare them and cal the differences
    val differ = AsyncListDiffer(this, differCallback)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): HomeViewHolder {
        val binding = ItemHomeLogsBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false)
        return HomeViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: HomeViewHolder,
        position: Int
    ) {
        val logItem = differ.currentList[position]

        holder.binding.apply {
            tvLogTitle.text = logItem.glucose?.notes ?: ""
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

}