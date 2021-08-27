package com.example.fancytimes

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.fancytimes.database.ReminderDatabase
import java.lang.Exception

class RefreshWorker(private val workerContext: Context, params: WorkerParameters) : Worker(workerContext, params) {
    override fun doWork(): Result {
        val dbInstance = ReminderDatabase.createInstance(workerContext)
        val reminderDao = dbInstance.reminderDao
        return try {
            val reminders = reminderDao.getAllReminders()
            refreshReminders(reminders, workerContext)
            Result.success()
        } catch (e: Exception) {
            println("Worker failed")
            Result.failure()
        }
    }

}