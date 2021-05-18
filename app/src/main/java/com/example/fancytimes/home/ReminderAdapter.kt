package com.example.fancytimes.home

import android.app.AlarmManager
import android.app.AlertDialog
import android.app.PendingIntent
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.fancytimes.FancyTimeBroadcast
import com.example.fancytimes.R
import com.example.fancytimes.database.Reminder
import com.example.fancytimes.database.ReminderDatabase
import com.example.fancytimes.databinding.ReminderListItemBinding
import java.util.*

class ReminderAdapter(
    private val preferences: SharedPreferences?,
    private val is24hrs: Boolean,
    private val viewModel: HomeViewModel,
    private val lifecycleOwner: LifecycleOwner,
    private val isSelectActive: MutableLiveData<Boolean>,
    private val isDirectSelectAll: MutableLiveData<Boolean>,
    private val isSelectAll: MutableLiveData<Boolean>,
    private val selectCount: MutableLiveData<Int>,
    private val isRemovalReady: MutableLiveData<Boolean>,
    private val alarmManager: AlarmManager
) :
    ListAdapter<Reminder, ReminderAdapter.ReminderViewHolder>(ReminderDiffCallback()) {

    init {
        println("Adapter init")
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReminderViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ReminderListItemBinding.inflate(inflater, parent, false)

        return ReminderViewHolder(binding)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onBindViewHolder(holder: ReminderViewHolder, position: Int) {
        val reminder = getItem(position)
        val rvContext = holder.itemView.context

        // this one's kinda messy, but I think all the string templates save a few if else branches, it's ok
        // it basically just formats the time according to the system time settings (24hours or not) as well as times below 10 (adding a 0)
        val hourText = if (is24hrs) {
//            println("reminder.hour = ${reminder.hour}")
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

        val monthText = when (reminder.month) {
            0 -> rvContext.getString(R.string.january)
            1 -> rvContext.getString(R.string.february)
            2 -> rvContext.getString(R.string.march)
            3 -> rvContext.getString(R.string.april)
            4 -> rvContext.getString(R.string.may)
            5 -> rvContext.getString(R.string.june)
            6 -> rvContext.getString(R.string.july)
            7 -> rvContext.getString(R.string.august)
            8 -> rvContext.getString(R.string.september)
            9 -> rvContext.getString(R.string.october)
            10 -> rvContext.getString(R.string.november)
            11 -> rvContext.getString(R.string.december)
            else -> "error"
        }

        val yearText = if (reminder.year == Calendar.getInstance().get(Calendar.YEAR)) "" else ", ${reminder.year}"

        holder.titleField.text = reminder.title
        holder.timeField.text = hourText
        if (reminder.isRepeating) holder.timeField.setCompoundDrawablesWithIntrinsicBounds(
            null,
            null,
            ContextCompat.getDrawable(rvContext, R.drawable.repeat_24px),
            null
        )
        holder.dateField.text =
            "$monthText ${if (reminder.day < 10) "0${reminder.day}" else reminder.day}$yearText"

//        println("Adapter request code: ${reminder.requestCode}")

        isSelectActive.observe(lifecycleOwner, {
            if (it) {
                holder.checkBox.visibility = View.VISIBLE
                selectCount.value = 0
            } else if (!it) {
                holder.checkBox.visibility = View.GONE
                holder.checkBox.isChecked = false
            }
        })

        isDirectSelectAll.observe(lifecycleOwner, {
            if (!it) {
                println("holder observed")
                if (holder.checkBox.isChecked) {
                    println("holder unchecked")

//                    reminder.selected = false
                }
                holder.checkBox.isChecked = false
                if (selectCount.value == 0) {
                    isDirectSelectAll.value = true
                }
            }
        })

        isSelectAll.observe(lifecycleOwner, {
            if (it) {
                println("holder")
                println(this.itemCount)
                if (!holder.checkBox.isChecked) {
                    println("holder checked")
                    holder.checkBox.isChecked = true
//                    reminder.selected = true
                }
            }
        })

        isRemovalReady.observe(lifecycleOwner, {
            println("removal ready")
            if (it) {
                val intent = Intent(rvContext, FancyTimeBroadcast::class.java)
//                    println("Request code key $requestCodeMax")
//                for (i in reminderAdapter.currentList) {
//                    println("selected")
                if (holder.checkBox.isChecked) {
                    try {
                        alarmManager.cancel(
                            PendingIntent.getBroadcast(
                                rvContext,
                                reminder.requestCode,
                                intent,
                                PendingIntent.FLAG_NO_CREATE
                            )
                        )
                        viewModel.deleteByRequestCode(reminder.requestCode)
                    } catch (e: Exception) {
                        println("cancel() called with a null PendingIntent")
                    }
                    with(preferences!!.edit()) {
                        this.remove(reminder.requestCode.toString())
                        this.apply()
                    }
                }
            } else if (!it) {
                println(selectCount.value)
                println(this.itemCount)
            }
        })

        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                println("checked")
                if (selectCount.value!! < this.itemCount) {
                    selectCount.value = selectCount.value?.plus(1)
                    if (selectCount.value!! == this.itemCount) {
                        println("error")
                        if (isSelectAll.value == false) {
                            isSelectAll.value = true
                        }
                    }
                }
            } else if (!isChecked) {
                println("unchecked")
                selectCount.value = selectCount.value?.minus(1)
                if (isSelectAll.value == true) {
                    isSelectAll.value = false
                }
            }
        }

        holder.removeButton.setOnClickListener {
            val alarmManager = it.context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            AlertDialog.Builder(it.context).setTitle("Cancel Reminder")
                .setMessage(context.getString(R.string.cancel_confirmation_specific)).setPositiveButton(
                    rvContext.getString(R.string.yes)
                ) { _: DialogInterface, _: Int ->
                    isSelectActive.value = false
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
                .setNegativeButton(rvContext.getString(R.string.no), null).setIcon(android.R.drawable.ic_dialog_alert).show()
        }

        holder.editButton.setOnClickListener {
            isSelectActive.value = false
            it.findNavController()
                .navigate(HomeFragmentDirections.actionHomeFragmentToDetailFragment(reminder.requestCode))
        }

        holder.listItem.setBackgroundColor(reminder.color)
        holder.timeField.setBackgroundColor(reminder.color)
        holder.titleField.setBackgroundColor(reminder.color)
        holder.removeButton.setBackgroundColor(reminder.color)
        holder.editButton.setBackgroundColor(reminder.color)
    }

    class ReminderViewHolder(reminderListItemBinding: ReminderListItemBinding) :
        RecyclerView.ViewHolder(reminderListItemBinding.root) {
        val titleField = reminderListItemBinding.tvTitle
        val timeField = reminderListItemBinding.tvTime
        val dateField = reminderListItemBinding.tvDate
        val removeButton = reminderListItemBinding.ibRemove
        val editButton = reminderListItemBinding.ibEdit
        val listItem = reminderListItemBinding.clListItem
        val checkBox = reminderListItemBinding.cbSelect
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