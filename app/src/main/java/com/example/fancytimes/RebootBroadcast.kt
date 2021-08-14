package com.example.fancytimes

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.fancytimes.database.Reminder
import com.example.fancytimes.database.ReminderDatabase
import com.example.fancytimes.home.HomeViewModel
import java.lang.Exception

class RebootBroadcast : BroadcastReceiver() {
    override fun onReceive(callingContext: Context?, callingIntent: Intent?) {

        // this if-check is only to be safe - it should always be true
        if (callingIntent?.action == Intent.ACTION_BOOT_COMPLETED) {
            println("action boot completedx")
            // TODO can't access the database like this, reminders will be null
            val reminderDao = ReminderDatabase.createInstance(callingContext!!).reminderDao
            var reminders: List<Reminder>
            try {
                reminders = reminderDao.getAllReminders().value!!
            } catch (e: Exception) {
                println("reminders is null")
                reminders = listOf()
                println(reminders.isEmpty())
            }
            refreshReminders(reminders, callingContext)
        }
    }
}