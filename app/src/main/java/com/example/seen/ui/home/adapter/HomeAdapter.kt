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
import com.example.seen.domain.model.entites.Reminder
import com.example.seen.util.Constants.Companion.HIGH_GLUCOSE_VALUE
import com.example.seen.util.Constants.Companion.LOW_GLUCOSE_VALUE
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
            tvLogReadingTime.text = fromLongToHour(logItem.log.logged_at)

            val glucoseValue = logItem.glucose?.glucose_level

            // Handle text
            tvLogReadingValue.text = glucoseValue?.let { "${it} mg/dl" }
                ?: context.getString(R.string.no_blood_reading)

            // Handle glucose UI
            if (glucoseValue == null) {
                ivGlucose.visibility = View.GONE

                tvLogReadingValue.background =
                    ContextCompat.getDrawable(context, R.drawable.bg_blood_not_exist_reading)

                tvLogReadingValue.setTextColor(
                    ContextCompat.getColor(context, R.color.description)
                )
            } else {
                val (bgRes, colorRes) = getSugarStyle(glucoseValue.toInt())

                tvLogReadingValue.background =
                    ContextCompat.getDrawable(context, bgRes)

                tvLogReadingValue.setTextColor(
                    ContextCompat.getColor(context, colorRes)
                )
            }

            // Handle other icons
            ivMeal.visibility = if (logItem.meal == null) View.GONE else View.VISIBLE
            ivMedicine.visibility = if (logItem.medication == null) View.GONE else View.VISIBLE

            val medicineParams = ivMedicine.layoutParams as ViewGroup.MarginLayoutParams
            val mealParams = ivMeal.layoutParams as ViewGroup.MarginLayoutParams

            if (glucoseValue == null && logItem.medication != null) {
                medicineParams.marginStart = 0
            }

            if (logItem.medication == null && logItem.meal != null) {
                mealParams.marginStart = 0
            }

            ivMedicine.layoutParams = medicineParams
            ivMeal.layoutParams = mealParams

            root.setOnClickListener {
                onItemClickListener?.invoke(logItem)
            }
        }
    }

    private fun fromLongToHour(timestamp: Long) =
        SimpleDateFormat("h:mm a").format(Date(timestamp))

    private fun getSugarStyle(value: Int): Pair<Int, Int> = when {
        value in LOW_GLUCOSE_VALUE..HIGH_GLUCOSE_VALUE -> R.drawable.bg_blood_good_reading to R.color.good_sugar_reading
        else -> R.drawable.bg_blood_bad_reading to R.color.bad_sugar_reading
    }


    private var onItemClickListener: ((FullLog) -> Unit)? = null

    fun setOnItemClickListener(listener: (FullLog) -> Unit) {
        onItemClickListener = listener
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

}