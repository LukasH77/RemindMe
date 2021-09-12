package com.example.fancytimes

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.fancytimes.database.Reminder
import com.example.fancytimes.database.ReminderDatabase
import com.example.fancytimes.home.HomeViewModel
import java.util.*

class StopActionBroadcast : BroadcastReceiver() {

    override fun onReceive(callingContext: Context?, callingIntent: Intent?) {

        val databaseReference =
            HomeViewModel(ReminderDatabase.createInstance(callingContext!!).reminderDao)

        val alarmManager = callingContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val notificationRequestCode = callingIntent!!.getIntExtra(
            callingContext.getString(R.string.notification_requestCode_extra_name),
            -1
        )
        val notificationTitle =
            callingIntent.getStringExtra(callingContext.getString(R.string.notification_title_extra_name))
        val notificationText =
            callingIntent.getStringExtra(callingContext.getString(R.string.notification_text_extra_name))
        val currentChannel = callingIntent.getIntExtra(
            callingContext.getString(R.string.notification_channel_count_extra_name),
            -1
        )

        try {
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

        val deleteActionIntent = Intent(callingContext, DeleteActionBroadcast::class.java)
        deleteActionIntent.putExtra(
            callingContext.getString(R.string.notification_requestCode_extra_name),
            notificationRequestCode
        )
        deleteActionIntent.putExtra(
            callingContext.getString(R.string.notification_channel_count_extra_name),
            currentChannel
        )
        deleteActionIntent.action = System.currentTimeMillis().toString()
        val deleteActonPendingIntent = PendingIntent.getBroadcast(
            callingContext,
            notificationRequestCode,
            deleteActionIntent,
            0
        )


        val notificationClickIntent = Intent(callingContext, MainActivity::class.java)
        notificationClickIntent.putExtra(
            callingContext.getString(R.string.notification_click_identifier_extra_name),
            1
        )
        notificationClickIntent.putExtra(
            callingContext.getString(R.string.notification_requestCode_extra_name),
            notificationRequestCode
        )
        notificationClickIntent.action = System.currentTimeMillis().toString()
        val notificationClickPendingIntent: PendingIntent = PendingIntent.getActivity(
            callingContext,
            notificationRequestCode,
            notificationClickIntent,
            0
        )

        val ns = Context.NOTIFICATION_SERVICE
        val notificationManager = callingContext.getSystemService(ns) as NotificationManager
        notificationManager.cancel(currentChannel)

        val notification =
            Notification/*Compat*/.Builder(
                callingContext,
                callingContext.getString(R.string.notification_channel_silent)
            ).setSmallIcon(R.drawable.access_time_24px)
                .setContentTitle(notificationTitle).setContentText(notificationText)
                .setStyle(Notification/*Compat*/.BigTextStyle().bigText(notificationText))
                .setContentIntent(notificationClickPendingIntent)
                .setShowWhen(true)
                .setAutoCancel(true)
                .addAction(
                    R.drawable.outline_remove_circle_outline_24,
                    callingContext.getString(R.string.delete_reminder),
                    deleteActonPendingIntent
                )
        with(NotificationManagerCompat.from(callingContext)) {
            notify(currentChannel, notification.build())
        }

        databaseReference.updateIsCancelled(notificationRequestCode, true)
    }
}