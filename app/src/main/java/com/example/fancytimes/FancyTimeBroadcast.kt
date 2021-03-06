package com.example.fancytimes

import android.app.AlarmManager
import android.app.Notification
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.fancytimes.database.Reminder
import com.example.fancytimes.database.ReminderDatabase
import com.example.fancytimes.home.HomeViewModel
import java.util.*

class FancyTimeBroadcast : BroadcastReceiver() {

    override fun onReceive(callingContext: Context?, callingIntent: Intent?) {

        val databaseReference =
            HomeViewModel(ReminderDatabase.createInstance(callingContext!!).reminderDao)
//
        val notificationTitle =
            callingIntent!!.getStringExtra(callingContext.getString(R.string.notification_title_extra_name))
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
            -1
        )
        var notificationTime = callingIntent.getLongExtra(
            callingContext.getString(R.string.notification_time_extra_name),
            0
        )
        val notificationColor =
            callingIntent.getIntExtra(callingContext.getString(R.string.context_extra_name), 0)
        val currentChannel = callingIntent.getIntExtra(
            callingContext.getString(R.string.notification_channel_count_extra_name),
            0
        )

//        println("Current notification channel notify: $currentChannel")

        val calendar = Calendar.getInstance()

//        println("repInterval: $notificationRepeatInterval")

        calendar.timeInMillis = notificationTime

//        println("IsRepeatingReceived: $isNotificationRepeating")
//        println("Request code: $notificationRequestCode")

//        val notificationChannels = callingContext.resources.getStringArray(R.array.notificationChannels)

        val powerManager = callingContext.getSystemService(Context.POWER_SERVICE) as PowerManager
        val isScreenOn = powerManager.isInteractive

        if (!isScreenOn) {
            val wakeLock = powerManager.newWakeLock(
                PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
                "reminders:notificationLock"
            )
            wakeLock.acquire(2500)
        }

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

//        println("Current channel: $currentChannel")
        if (isNotificationRepeating) {
            val stopActionIntent = Intent(callingContext, StopActionBroadcast::class.java)
            stopActionIntent.putExtra(
                callingContext.getString(R.string.notification_requestCode_extra_name),
                notificationRequestCode
            )
            stopActionIntent.putExtra(
                callingContext.getString(R.string.notification_title_extra_name),
                notificationTitle
            )
            stopActionIntent.putExtra(
                callingContext.getString(R.string.notification_text_extra_name),
                notificationText
            )
            stopActionIntent.putExtra(
                callingContext.getString(R.string.notification_channel_count_extra_name),
                currentChannel
            )
            stopActionIntent.action = System.currentTimeMillis().toString()
            val stopActonPendingIntent = PendingIntent.getBroadcast(
                callingContext,
                notificationRequestCode,
                stopActionIntent,
                0
            )

            val notification =
                NotificationCompat.Builder(
                    callingContext,
                    callingContext.getString(R.string.notification_channel)
                ).setSmallIcon(R.drawable.access_time_24px)
                    .setContentTitle(notificationTitle).setContentText(notificationText)
                    .setStyle(NotificationCompat.BigTextStyle().bigText(notificationText))
                    .setContentIntent(notificationClickPendingIntent)
                    .setShowWhen(true)
                    .setAutoCancel(true)
                    .addAction(
                        R.drawable.outline_remove_circle_outline_24,
                        callingContext.getString(R.string.stop_repeating),
                        stopActonPendingIntent
                    )
                    .addAction(
                        R.drawable.outline_remove_circle_outline_24,
                        callingContext.getString(R.string.delete_reminder),
                        deleteActonPendingIntent
                    )
            with(NotificationManagerCompat.from(callingContext)) {
                notify(currentChannel, notification.build())
            }

            if (notificationRepeatInterval != 1L && notificationRepeatInterval != 2L) {
                notificationTime += notificationRepeatInterval
                while (notificationTime <= Calendar.getInstance().timeInMillis) notificationTime += notificationRepeatInterval
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
                    notificationColor,
                    currentChannel,
                    false
                )
            )
        } else {
            val notification =
                NotificationCompat.Builder(
                    callingContext,
                    callingContext.getString(R.string.notification_channel)
                ).setSmallIcon(R.drawable.access_time_24px)
                    .setContentTitle(notificationTitle).setContentText(notificationText)
                    .setStyle(NotificationCompat.BigTextStyle().bigText(notificationText))
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
                    notificationColor,
                    currentChannel,
                    true
                )
            )
        }
    }
}