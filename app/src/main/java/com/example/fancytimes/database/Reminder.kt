package com.example.fancytimes.database

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Reminder(
    @PrimaryKey
    val requestCode: Int,

    @ColumnInfo
    val title: String,

    @ColumnInfo
    val text: String,

    @ColumnInfo
    val timeInMillis: Long,

    @ColumnInfo
    val minute: Int,

    @ColumnInfo
    val hour: Int,

    @ColumnInfo
    val day: Int,

    @ColumnInfo
    val month: Int,

    @ColumnInfo
    val year: Int,

    @ColumnInfo
    val repetition: Long?,

    @ColumnInfo
    val isRepeating: Boolean,

    @ColumnInfo
    val color: Int,

    @ColumnInfo
    val notificationChannel: Int
)