package com.example.fancytimes.home

import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.recyclerview.widget.RecyclerView
import com.example.fancytimes.R

class ReminderAdapter(private val reminders: List<TestDataClass>) :
    RecyclerView.Adapter<ReminderAdapter.ReminderViewHolder>() {

    class ReminderViewHolder(reminderListItem: View) : RecyclerView.ViewHolder(reminderListItem) {
        val titleField: EditText = reminderListItem.findViewById(R.id.etTitle)
        val textField: EditText = reminderListItem.findViewById(R.id.etText)
        val timeField: EditText = reminderListItem.findViewById(R.id.etTime)
        val repeatField: EditText = reminderListItem.findViewById(R.id.etRepeat)
        val removeButton: Button = reminderListItem.findViewById(R.id.bRemove)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReminderViewHolder {
        val reminderListItem = LayoutInflater.from(parent.context).inflate(R.layout.reminder_list_item, parent, false)

        return ReminderViewHolder(reminderListItem)
    }

    override fun onBindViewHolder(holder: ReminderViewHolder, position: Int) {
        holder.titleField.text = SpannableStringBuilder(reminders[position].title)
        holder.textField.text = SpannableStringBuilder(reminders[position].text)
        holder.timeField.text = SpannableStringBuilder(reminders[position].timeInMillis.toString())
        holder.repeatField.text = SpannableStringBuilder(reminders[position].repetition.toString())
    }

    override fun getItemCount(): Int = reminders.size
}