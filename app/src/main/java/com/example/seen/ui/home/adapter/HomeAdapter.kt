package com.example.seen.ui.home.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.seen.R
import com.example.seen.databinding.ItemHomeLogsBinding
import com.example.seen.domain.model.entites.FullLog
import java.text.SimpleDateFormat
import java.util.Date

class HomeAdapter(
    val context: Context
): RecyclerView.Adapter<HomeAdapter.HomeViewHolder>() {

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
            tvLogTitle.text = logItem.log.log_title
            tvLogDescription.text = logItem.log.log_description
            tvLogReadingTime.text = fromLongToHour(logItem.log.created_at)

            if(logItem.glucose == null){
                ivGlucose.visibility = View.GONE

                tvLogReadingValue.background = ContextCompat.getDrawable(context,R.drawable.bg_blood_not_exist_reading)
                tvLogReadingValue.setTextColor(context.getColor(R.color.description))
                tvLogReadingValue.text = context.getString(R.string.no_blood_reading)
            }
            else{

            }

            if (logItem.meal == null) {
                ivMeal.visibility = View.GONE
            }
            if (logItem.medication == null) {
                ivMedication.visibility = View.GONE
            }
        }
    }

    private fun fromLongToHour(timestamp: Long) =
        SimpleDateFormat("h:mm a").format(Date(timestamp))

    private fun setLogReadingBackground(value: Int, binding: ItemHomeLogsBinding) {

    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

}