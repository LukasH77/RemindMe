package com.example.fancytimes.database

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert
import androidx.room.Query;

@Dao
interface ReminderDao {
    @Insert
    fun addReminder(reminder: Reminder)

    @Query("SELECT * FROM Reminder ORDER BY timeInMillis ASC")
    fun getAllReminders(): LiveData<List<Reminder>>

    @Query("DELETE FROM Reminder WHERE requestCode = :requestCode")
    fun deleteByRequestCode(requestCode: Int)

    @Query("DELETE FROM Reminder")
    fun deleteAll()
}