package com.example.seen.ui.reminder.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.seen.R
import com.example.seen.databinding.ItemReminderBinding
import com.example.seen.domain.model.entites.Reminder
import java.text.SimpleDateFormat
import java.util.Date

class ReminderAdapter(
    val context: Context
): RecyclerView.Adapter<ReminderAdapter.ReminderViewHolder>() {

    inner class ReminderViewHolder(val binding: ItemReminderBinding) : RecyclerView.ViewHolder(binding.root)

    //we will use differ list because its more accurate : Because it compare between two lists in the background so it wont update the list every time
    private val differCallback = object : DiffUtil.ItemCallback<Reminder>(){

        override fun areItemsTheSame(
            oldItem: Reminder,
            newItem: Reminder
        )
                : Boolean {
            return oldItem.reminder_id == newItem.reminder_id
        }

        override fun areContentsTheSame(
            oldItem: Reminder,
            newItem: Reminder
        )
        : Boolean {
            return oldItem == newItem
        }
    }

    //AsyncListDiffer is a tool that takes two list and compare them and cal the differences
    val differ = AsyncListDiffer(this, differCallback)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ReminderViewHolder {
        val binding = ItemReminderBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false)
        return ReminderViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: ReminderViewHolder,
        position: Int
    ) {
        val reminderItem = differ.currentList[position]

        holder.binding.apply {
            if (reminderItem.message_type == "medication"){
                tvReminderTitle.text = reminderItem.medication_name
            } else {
                tvReminderTitle.text = reminderItem.message
            }

            ivReminderType.setImageResource(setReminderTypeImage(reminderItem.message_type))
            tvReminderTime.text = context.getString(R.string.schedule_in) + " " + SimpleDateFormat("hh:mm a").format(
                Date(reminderItem.time)
            )

            cardDelete.setOnClickListener {
                onDeleteClickListener?.invoke(reminderItem)
            }

        }
    }

    private fun setReminderTypeImage(message_type : String) = when (message_type) {
        "medication" -> R.drawable.ic_medicine_mid_size
        "glucose" -> R.drawable.ic_glucose_mid_size
        else -> R.drawable.ic_meal_mid_size
    }

    private var onDeleteClickListener: ((Reminder) -> Unit)? = null

    fun setOnDeleteClickListener(listener: (Reminder) -> Unit) {
        onDeleteClickListener = listener
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

}