package com.example.fancytimes.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface ReminderDao {
    @Insert
    fun addReminder(reminder: Reminder)

    @Update
    fun updateReminder(reminder: Reminder)

    @Query("UPDATE Reminder SET isCancelled = :isCancelled WHERE requestCode = :requestCode")
    fun updateIsCancelled(requestCode: Int, isCancelled: Boolean)

    @Query("SELECT * FROM Reminder WHERE requestCode = :requestCode")
    fun getByRequestCode(requestCode:Int): LiveData<Reminder>

    @Query("SELECT * FROM Reminder ORDER BY timeInMillis ASC")
    fun getAllRemindersLive(): LiveData<List<Reminder>>

    @Query("SELECT * FROM Reminder ORDER BY timeInMillis ASC")
    fun getAllReminders(): List<Reminder>

    @Query("DELETE FROM Reminder WHERE requestCode = :requestCode")
    fun deleteByRequestCode(requestCode: Int)

    @Query("DELETE FROM Reminder")
    fun deleteAll()
}