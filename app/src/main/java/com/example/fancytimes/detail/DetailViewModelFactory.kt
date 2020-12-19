package com.example.fancytimes.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.fancytimes.database.ReminderDao

@Suppress("UNCHECKED_CAST")
class DetailViewModelFactory(private val reminderDao: ReminderDao, private val requestCode: Int) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DetailViewModel::class.java)) {
            return DetailViewModel(reminderDao, requestCode) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}