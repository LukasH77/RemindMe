package com.example.fancytimes.home

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import com.example.fancytimes.database.Reminder
import com.example.fancytimes.database.ReminderDao
import kotlinx.coroutines.*

class HomeViewModel(private val reminderDao: ReminderDao) : ViewModel() {
    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.Main + job)

    val reminders = reminderDao.getAllReminders()

    fun updateReminder(reminder: Reminder) {
        scope.launch {
            withContext(Dispatchers.IO) {
                reminderDao.updateReminder(reminder)
            }
        }
    }

    fun deleteAll() {
        scope.launch {
            withContext(Dispatchers.IO) {
                reminderDao.deleteAll()
            }
        }
    }

    fun deleteByRequestCode(requestCode: Int) {
        scope.launch {
            withContext(Dispatchers.IO) {
                reminderDao.deleteByRequestCode(requestCode)
            }
        }
    }
}