package com.example.fancytimes.setter

import androidx.lifecycle.ViewModel
import com.example.fancytimes.database.Reminder
import com.example.fancytimes.database.ReminderDao
import kotlinx.coroutines.*

class SetterViewModel(private val reminderDao: ReminderDao) : ViewModel() {
    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.Main + job)

    fun addReminder(reminder: Reminder) {
        scope.launch{
            withContext(Dispatchers.IO) {
                reminderDao.addReminder(reminder)
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
}