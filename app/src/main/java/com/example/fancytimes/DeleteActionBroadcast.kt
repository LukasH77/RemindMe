package com.example.fancytimes

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.example.fancytimes.database.ReminderDatabase
import com.example.fancytimes.home.HomeViewModel

class DeleteActionBroadcast : BroadcastReceiver() {

    override fun onReceive(callingContext: Context?, callingIntent: Intent?) {

        val databaseReference =
            HomeViewModel(ReminderDatabase.createInstance(callingContext!!).reminderDao)

        val alarmManager = callingContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val notificationRequestCode = callingIntent!!.getIntExtra(callingContext.getString(R.string.notification_requestCode_extra_name), -1)
        val currentChannel = callingIntent.getIntExtra(callingContext.getString(R.string.notification_channel_count_extra_name), -1)

        val ns = Context.NOTIFICATION_SERVICE
        val notificationManager = callingContext.getSystemService(ns) as NotificationManager
        notificationManager.cancel(currentChannel)

        try {
            databaseReference.deleteByRequestCode(notificationRequestCode)

            val intent = Intent(callingContext, FancyTimeBroadcast::class.java)

            alarmManager.cancel(
                PendingIntent.getBroadcast(
                    callingContext,
                    notificationRequestCode,
                    intent,
                    PendingIntent.FLAG_NO_CREATE
                )
            )
        } catch (e: Exception) {
//            println("error in DismissActionBroadcast")
        }
    }
}