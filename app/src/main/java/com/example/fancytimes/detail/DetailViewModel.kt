package com.example.fancytimes.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.fancytimes.database.Reminder
import com.example.fancytimes.database.ReminderDao
import kotlinx.coroutines.*

class DetailViewModel(private val reminderDao: ReminderDao, requestCode: Int) : ViewModel() {
    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.Main + job)

    val selectedReminder: LiveData<Reminder>

    init {
        selectedReminder = getByRequestCode(requestCode)
    }

    private fun getByRequestCode(requestCode: Int): LiveData<Reminder> = reminderDao.getByRequestCode(requestCode)


    fun updateReminder(reminder: Reminder) {
        scope.launch {
            withContext(Dispatchers.IO) {
                reminderDao.updateReminder(reminder)
            }
        }
    }
}