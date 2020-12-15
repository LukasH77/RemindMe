package com.example.fancytimes.home

import android.content.Context
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.fancytimes.R
import com.example.fancytimes.database.Reminder
import com.example.fancytimes.database.ReminderDatabase

class ReminderAdapter(/*private val reminders: List<Reminder>*/) :
    ListAdapter<Reminder, ReminderAdapter.ReminderViewHolder>(ReminderDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReminderViewHolder {
        val reminderListItem =
            LayoutInflater.from(parent.context).inflate(R.layout.reminder_list_item, parent, false)

        return ReminderViewHolder(reminderListItem)
    }

    override fun onBindViewHolder(holder: ReminderViewHolder, position: Int) {
        val reminder = getItem(position)
        holder.titleField.text = SpannableStringBuilder(reminder.title)
        holder.textField.text = SpannableStringBuilder(reminder.text)
        holder.timeField.text = SpannableStringBuilder(reminder.timeInMillis.toString())
        holder.repeatField.text = SpannableStringBuilder(reminder.repetition.toString())
        holder.requestCode.text = SpannableStringBuilder(reminder.requestCode.toString())
    }

    class ReminderViewHolder(reminderListItem: View) : RecyclerView.ViewHolder(reminderListItem) {
        val titleField: EditText = reminderListItem.findViewById(R.id.etTitle)
        val textField: EditText = reminderListItem.findViewById(R.id.etText)
        val timeField: EditText = reminderListItem.findViewById(R.id.etTime)
        val repeatField: EditText = reminderListItem.findViewById(R.id.etRepeat)
        val requestCode: TextView = reminderListItem.findViewById(R.id.tvRequestCode)
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