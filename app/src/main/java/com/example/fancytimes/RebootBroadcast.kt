package com.example.fancytimes

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.fancytimes.database.Reminder
import com.example.fancytimes.database.ReminderDatabase
import com.example.fancytimes.home.HomeViewModel
import java.lang.Exception

class RebootBroadcast : BroadcastReceiver() {
    override fun onReceive(callingContext: Context?, callingIntent: Intent?) {
        // this if-check is only to be safe - it should always be true
        if (callingIntent?.action == Intent.ACTION_BOOT_COMPLETED) {
            val reminderRefreshRequest = OneTimeWorkRequestBuilder<RefreshWorker>().build()
            WorkManager.getInstance(callingContext!!).enqueue(reminderRefreshRequest)
        }
    }
}