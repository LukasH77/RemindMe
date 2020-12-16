package com.example.fancytimes.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.fancytimes.R
import com.example.fancytimes.database.Reminder

class ReminderAdapter(/*private val reminders: List<Reminder>*/) :
    ListAdapter<Reminder, ReminderAdapter.ReminderViewHolder>(ReminderDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReminderViewHolder {
        val reminderListItem =
            LayoutInflater.from(parent.context).inflate(R.layout.reminder_list_item, parent, false)

        return ReminderViewHolder(reminderListItem)
    }

    override fun onBindViewHolder(holder: ReminderViewHolder, position: Int) {
        val reminder = getItem(position)
        holder.titleField.text = reminder.title
        holder.timeField.text = "${if (reminder.hour < 10) {"0${reminder.hour}"} else {reminder.hour}}" +
                ":${if (reminder.minute < 10) {"0${reminder.minute}"} else {reminder.minute}} ${reminder.day}.${reminder.month}.${reminder.year}  (${reminder.requestCode})"
    }

    class ReminderViewHolder(reminderListItem: View) : RecyclerView.ViewHolder(reminderListItem) {
        val titleField: TextView = reminderListItem.findViewById(R.id.tvTitle)
        val timeField: TextView = reminderListItem.findViewById(R.id.tvTime)
        val removeButton: Button = reminderListItem.findViewById(R.id.bRemove)
    }
}

class ReminderDiffCallback : DiffUtil.ItemCallback<Reminder>() {
    override fun areItemsTheSame(oldItem: Reminder, newItem: Reminder): Boolean {
        return oldItem.requestCode == newItem.requestCode
    }

    override fun areContentsTheSame(oldItem: Reminder, newItem: Reminder): Boolean {
        return oldItem == newItem
    }
}