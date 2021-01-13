package com.example.fancytimes.home

import android.app.AlarmManager
import android.app.AlertDialog
import android.app.PendingIntent
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.fancytimes.FancyTimeBroadcast
import com.example.fancytimes.R
import com.example.fancytimes.database.Reminder
import com.example.fancytimes.database.ReminderDatabase

class ReminderAdapter(private val preferences: SharedPreferences?, private val is24hrs: Boolean) :
    ListAdapter<Reminder, ReminderAdapter.ReminderViewHolder>(ReminderDiffCallback()) {

    init {
        println("Adapter init")
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReminderViewHolder {
        val reminderListItem =
            LayoutInflater.from(parent.context).inflate(R.layout.reminder_list_item, parent, false)

        return ReminderViewHolder(reminderListItem)
    }

    override fun onBindViewHolder(holder: ReminderViewHolder, position: Int) {
        val reminder = getItem(position)

        // this one's kinda messy, but I think all the string templates save a few if else branches, it's ok
        // it basically just formats the time according to the system time settings (24hours or not) as well as times below 10 (adding a 0)
        val hourText: String = if (is24hrs) {
            "${if (reminder.hour < 10) "0${reminder.hour}" else reminder.hour}:${if (reminder.minute < 10) "0${reminder.minute}" else "${reminder.minute}"}"
        } else {
            "${
                when (reminder.hour) {
                    0 -> "12"
                    in 1..12 -> reminder.hour.toString()
                    else -> (reminder.hour - 12).toString()
                }
            }:${
                if (reminder.hour < 12) {
                    if (reminder.minute < 10) "0${reminder.minute} AM" else "${reminder.minute} AM"
                } else {
                    if (reminder.minute < 10) "0${reminder.minute} PM" else "${reminder.minute} PM"
                }
            }"
        }

        holder.titleField.text = reminder.title
        holder.timeField.text =
            "$hourText ${if (reminder.day < 10) "0${reminder.day}" else reminder.day}.${if (reminder.month < 9) "0${reminder.month + 1}" else reminder.month + 1}.${reminder.year}"

        println("Adapter request code: ${reminder.requestCode}")

        holder.removeButton.setOnClickListener {
            val alarmManager = it.context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            AlertDialog.Builder(it.context).setTitle("Cancel Reminder")
                .setMessage("Do you really want to cancel this reminder?").setPositiveButton(
                    "Yes"
                ) { _: DialogInterface, _: Int ->
                    val intent = Intent(it.context, FancyTimeBroadcast::class.java)
                    try {
                        alarmManager.cancel(
                            PendingIntent.getBroadcast(
                                it.context,
                                reminder.requestCode,
                                intent,
                                PendingIntent.FLAG_NO_CREATE
                            )
                        )
                        HomeViewModel(ReminderDatabase.createInstance(it.context).reminderDao).deleteByRequestCode(
                            reminder.requestCode
                        )
                        with(preferences!!.edit()) {
                            this.remove(reminder.requestCode.toString())
                            this.apply()
                        }
                    } catch (e: Exception) {
                        println("cancel() called with a null PendingIntent")
                    }
                }
                .setNegativeButton("No", null).setIcon(android.R.drawable.ic_dialog_alert).show()
        }

        holder.editButton.setOnClickListener {
            it.findNavController()
                .navigate(HomeFragmentDirections.actionHomeFragmentToDetailFragment(reminder.requestCode))
        }

        holder.listItem.setBackgroundColor(reminder.color)
        holder.timeField.setBackgroundColor(reminder.color)
        holder.titleField.setBackgroundColor(reminder.color)
        holder.removeButton.setBackgroundColor(reminder.color)
        holder.editButton.setBackgroundColor(reminder.color)
    }

    class ReminderViewHolder(reminderListItem: View) : RecyclerView.ViewHolder(reminderListItem) {
        val titleField: TextView = reminderListItem.findViewById(R.id.tvTitle)
        val timeField: TextView = reminderListItem.findViewById(R.id.tvTime)
        val removeButton: ImageButton = reminderListItem.findViewById(R.id.ibRemove)
        val editButton: ImageButton = reminderListItem.findViewById(R.id.ibEdit)
        val listItem: ConstraintLayout = reminderListItem.findViewById(R.id.clListItem)
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