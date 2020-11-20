package com.example.fancytimes

import android.app.AlarmManager
import android.app.Notification
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationManagerCompat

class FancyTimeBroadcast() : BroadcastReceiver() {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onReceive(callingContext: Context?, callingIntent: Intent?) {

        val notificationClickIntent = Intent(callingContext, MainActivity::class.java)

        val notificationClickPendingIntent: PendingIntent =
            PendingIntent.getActivity(callingContext, 0, notificationClickIntent, 0)

        val notificationTitle =
            callingIntent!!.getStringExtra(callingContext!!.getString(R.string.notification_title_extra_name))
        val notificationText =
            callingIntent.getStringExtra(callingContext.getString(R.string.notification_text_extra_name))
        val isNotificationRepeating = callingIntent.getBooleanExtra(
            callingContext.getString(R.string.notification_repeat_extra_name),
            false
        )
        val notificationRequestCode = callingIntent.getIntExtra(
            callingContext.getString(R.string.notification_requestCode_extra_name),
            0
        )
        var notificationTime = callingIntent.getLongExtra(
            callingContext.getString(R.string.notification_time_extra_name),
            0
        )

        println("Request code: $notificationRequestCode")

        val notification =
            Notification.Builder(
                callingContext,
                callingContext.getString(R.string.notification_channel_id)
            )
                .setSmallIcon(R.drawable.access_time_24px)
                .setContentTitle(notificationTitle).setContentText(notificationText)
                .setStyle(Notification.BigTextStyle().bigText(notificationText))
                .setContentIntent(notificationClickPendingIntent)
                .setAutoCancel(true)
        with(NotificationManagerCompat.from(callingContext)) {
            notify(1, notification.build())
        }

        if (isNotificationRepeating) {
            notificationTime += 60000
            val alarmManager =
                callingContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            callingIntent.putExtra(
                callingContext.getString(R.string.notification_time_extra_name),
                notificationTime
            )

            val pendingIntent =
                PendingIntent.getBroadcast(callingContext, notificationRequestCode, callingIntent, FLAG_UPDATE_CURRENT)

            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                notificationTime,
                pendingIntent
            )
        }
    }
}