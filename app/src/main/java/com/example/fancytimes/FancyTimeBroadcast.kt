package com.example.fancytimes

import android.app.Notification
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationManagerCompat
import com.example.fancytimes.home.HomeFragment

class FancyTimeBroadcast : BroadcastReceiver() {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onReceive(p0: Context?, p1: Intent?) {

        val intent = Intent(p0, MainActivity::class.java)

        val pendingIntent: PendingIntent = PendingIntent.getActivity(p0, 0, intent, 0)

        val notificationTitle = p1!!.getStringExtra(p0!!.getString(R.string.notification_title_extra_name))
        val notificationText = p1.getStringExtra(p0.getString(R.string.notification_text_extra_name))

        val notification =
            Notification.Builder(p0, p0.getString(R.string.notification_channel_id))
                .setSmallIcon(R.drawable.access_time_24px)
                .setContentTitle(notificationTitle).setContentText(notificationText)
                .setStyle(Notification.BigTextStyle().bigText(notificationText))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
        with(NotificationManagerCompat.from(p0)) {
            notify(1, notification.build())
        }
    }
}