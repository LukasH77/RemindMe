package com.example.fancytimes.home

import androidx.lifecycle.ViewModel
import com.example.fancytimes.database.ReminderDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job

class HomeViewModel(val reminderDao: ReminderDao) : ViewModel() {
    private val job = Job()
    val scope = (Dispatchers.Main + job)


}