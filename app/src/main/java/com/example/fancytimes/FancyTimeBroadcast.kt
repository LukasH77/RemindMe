package com.example.fancytimes

import android.app.Notification
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationManagerCompat

class FancyTimeBroadcast : BroadcastReceiver() {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onReceive(p0: Context?, p1: Intent?) {
        val notification =
            Notification.Builder(p0, "FancyTimes notifications")
                .setSmallIcon(R.drawable.access_time_24px)
                .setContentTitle("Fancy Time!!").setContentText("It's time, it is fancy o'clock!")
        with(NotificationManagerCompat.from(p0!!)) {
            notify(1, notification.build())
        }
    }
}