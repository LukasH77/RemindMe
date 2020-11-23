package com.example.fancytimes.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Reminder(
    @PrimaryKey(autoGenerate = true)
    val requestCode: Int,

    @ColumnInfo
    val title: String,

    @ColumnInfo
    val text: String,

    @ColumnInfo
    val timeInMillis: Long,

    @ColumnInfo
    val repetition: Long?
)