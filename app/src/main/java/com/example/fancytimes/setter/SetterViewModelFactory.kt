package com.example.fancytimes.setter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.fancytimes.database.ReminderDao
import com.example.fancytimes.home.HomeViewModel

@Suppress("UNCHECKED_CAST")
class SetterViewModelFactory(private val reminderDao: ReminderDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SetterViewModel::class.java)) {
            return SetterViewModel(reminderDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}