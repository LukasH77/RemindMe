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
import com.example.fancytimes.database.Reminder
import com.example.fancytimes.database.ReminderDatabase
import com.example.fancytimes.home.HomeViewModel
import java.util.*

class FancyTimeBroadcast() : BroadcastReceiver() {


    // TODO 2. DONE
    //  -> Non-repeating alarms delete their preference after triggering


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onReceive(callingContext: Context?, callingIntent: Intent?) {

        val preferences = callingContext?.getSharedPreferences(
            callingContext.getString(R.string.notification_preferences_key),
            Context.MODE_PRIVATE
        )

        val databaseReference =
            HomeViewModel(ReminderDatabase.createInstance(callingContext!!).reminderDao)

        with(preferences!!.edit()) {
            this.remove("requestCode")
            this.apply()
        }

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
        val notificationRepeatInterval = callingIntent.getLongExtra(
            callingContext.getString(R.string.notification_repeat_interval_extra_name),
            0
        )
        val notificationRequestCode = callingIntent.getIntExtra(
            callingContext.getString(R.string.notification_requestCode_extra_name),
            0
        )
        var notificationTime = callingIntent.getLongExtra(
            callingContext.getString(R.string.notification_time_extra_name),
            0
        )
        val notificationColor =
            callingIntent.getIntExtra(callingContext.getString(R.string.context_extra_name), 0)

        val calendar = Calendar.getInstance()

        println("repInterval: $notificationRepeatInterval")

        calendar.timeInMillis = notificationTime

        println("IsRepeatingReceived: $isNotificationRepeating")
//        println("Request code: $notificationRequestCode")

        val notification =
            Notification.Builder(
                callingContext,
                callingContext.getString(R.string.notification_channel_id)
            ).setSmallIcon(R.drawable.access_time_24px)
                .setContentTitle(notificationTitle).setContentText(notificationText)
                .setStyle(Notification.BigTextStyle().bigText(notificationText))
                .setContentIntent(notificationClickPendingIntent)
                .setShowWhen(true)
                .setAutoCancel(true)
        with(NotificationManagerCompat.from(callingContext)) {
            notify(1, notification.build())
        }

        if (isNotificationRepeating) {
            if (notificationRepeatInterval != 1L && notificationRepeatInterval != 2L) {
                notificationTime += notificationRepeatInterval
                calendar.timeInMillis = notificationTime
            } else {
                if (notificationRepeatInterval == 1L) {
                    calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) + 1)
                } else if (notificationRepeatInterval == 2L) {
                    calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR) + 1)
                }
                notificationTime = calendar.timeInMillis
            }

            val alarmManager =
                callingContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            callingIntent.putExtra(
                callingContext.getString(R.string.notification_time_extra_name),
                notificationTime
            )

            val pendingIntent =
                PendingIntent.getBroadcast(
                    callingContext,
                    notificationRequestCode,
                    callingIntent,
                    FLAG_UPDATE_CURRENT
                )

            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                notificationTime,
                pendingIntent
            )
            databaseReference.updateReminder(
                Reminder(
                    notificationRequestCode,
                    notificationTitle!!,
                    notificationText!!,
                    notificationTime,
                    calendar.get(Calendar.MINUTE),
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.DAY_OF_MONTH),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.YEAR),
                    notificationRepeatInterval,
                    isNotificationRepeating,
                    notificationColor
                )
            )
        } else {
            with(preferences.edit()) {
                this.remove(notificationRequestCode.toString())
                this.apply()
            }
            databaseReference.deleteByRequestCode(
                notificationRequestCode
            )
        }
    }
}