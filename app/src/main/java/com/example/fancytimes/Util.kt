package com.example.fancytimes

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.example.fancytimes.database.Reminder
import com.example.fancytimes.database.ReminderDatabase
import com.example.fancytimes.detail.DetailViewModel
import com.example.fancytimes.home.HomeFragment
import com.example.fancytimes.setter.SetterViewModel
import kotlinx.coroutines.*
import java.util.*

fun hideSoftKeyboard(context: Context, view: View) {
    val inputMethodManager =
        context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
}

fun handleAlarmsSetter(
    context: Context,
    viewModel: SetterViewModel,
    calendarInstance: Calendar,
    notificationMinute: Int,
    notificationHour: Int,
    notificationMillis: Long,
    preferencesMaybe: SharedPreferences?,
    notificationTitle: String,
    notificationText: String,
    isNotificationRepeating: Boolean,
    notificationRepeatInterval: Long,
    notificationColor: Int
) {
    val preferences = context.getSharedPreferences(
        context.getString(R.string.notification_preferences_key),
        Context.MODE_PRIVATE
    )

    val notificationRequestCode =
        preferences!!.getInt(context.getString(R.string.request_code_key), 0)
//    println("Request code: $notificationRequestCode")

    val currentChannel =
        preferences.getInt(context.getString(R.string.notification_channel_count), 0)

    val intent = Intent(context, FancyTimeBroadcast::class.java)
    intent.putExtra(
        context.getString(R.string.notification_title_extra_name),
        notificationTitle
    )
    intent.putExtra(context.getString(R.string.notification_text_extra_name), notificationText)
    intent.putExtra(
        context.getString(R.string.notification_repeat_extra_name),
        isNotificationRepeating
    )
    intent.putExtra(
        context.getString(R.string.notification_repeat_interval_extra_name),
        notificationRepeatInterval
    )
    intent.putExtra(
        context.getString(R.string.notification_requestCode_extra_name),
        notificationRequestCode
    )
    intent.putExtra(
        context.getString(R.string.notification_time_extra_name),
        notificationMillis
    )
    intent.putExtra(context.getString(R.string.context_extra_name), notificationColor)
    intent.putExtra(
        context.getString(R.string.notification_channel_count_extra_name),
        currentChannel
    )

//    println("Current notification channel set: $currentChannel")

    with(preferences.edit()) {
        this.putInt(context.getString(R.string.notification_channel_count), currentChannel + 1)
        this.apply()
    }

    val pendingIntent =
        PendingIntent.getBroadcast(context, notificationRequestCode, intent, 0)

    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    alarmManager.setExactAndAllowWhileIdle(
        AlarmManager.RTC_WAKEUP,
        notificationMillis,
        pendingIntent
    )

//    println(calendarInstance.get(Calendar.YEAR))

    viewModel.addReminder(
        Reminder(
            notificationRequestCode,
            notificationTitle,
            notificationText,
            notificationMillis,
            notificationMinute,
            notificationHour,
            calendarInstance.get(Calendar.DAY_OF_MONTH),
            calendarInstance.get(Calendar.MONTH),
            calendarInstance.get(Calendar.YEAR),
            notificationRepeatInterval,
            isNotificationRepeating,
            notificationColor,
            currentChannel,
            false
        )
    )

    with(preferences.edit()) {
        this.putInt(context.getString(R.string.request_code_key), notificationRequestCode + 1)
//        this.putInt(notificationRequestCode.toString(), notificationRequestCode)
        this.apply()
    }
//    Toast.makeText(context, currentChannel.toString(), Toast.LENGTH_SHORT)
//        .show()
}

fun handleAlarmsDetail(
    context: Context,
    viewModel: DetailViewModel,
    notificationRequestCode: Int,
    calendarInstance: Calendar,
    notificationMinute: Int,
    notificationHour: Int,
    notificationMillis: Long,
    notificationTitle: String,
    notificationText: String,
    isNotificationRepeating: Boolean,
    notificationRepeatInterval: Long,
    notificationColor: Int,
    currentChannel: Int
) {
    val preferences = context.getSharedPreferences(
        context.getString(R.string.notification_preferences_key),
        Context.MODE_PRIVATE
    )

    val intent = Intent(context, FancyTimeBroadcast::class.java)

//    println("IsRepeatingPassed $isNotificationRepeating")

    intent.putExtra(
        context.getString(R.string.notification_title_extra_name),
        notificationTitle
    )
    intent.putExtra(context.getString(R.string.notification_text_extra_name), notificationText)
    intent.putExtra(
        context.getString(R.string.notification_repeat_extra_name),
        isNotificationRepeating
    )
    intent.putExtra(
        context.getString(R.string.notification_repeat_interval_extra_name),
        notificationRepeatInterval
    )
    intent.putExtra(
        context.getString(R.string.notification_requestCode_extra_name),
        notificationRequestCode
    )
    intent.putExtra(
        context.getString(R.string.notification_time_extra_name),
        notificationMillis
    )
    intent.putExtra(context.getString(R.string.context_extra_name), notificationColor)
    intent.putExtra(
        context.getString(R.string.notification_channel_count_extra_name),
        currentChannel
    )

    val pendingIntent =
        PendingIntent.getBroadcast(
            context,
            notificationRequestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    alarmManager.setExactAndAllowWhileIdle(
        AlarmManager.RTC_WAKEUP,
        notificationMillis,
        pendingIntent
    )

    viewModel.updateReminder(
        Reminder(
            notificationRequestCode,
            notificationTitle,
            notificationText,
            notificationMillis,
            notificationMinute,
            notificationHour,
            calendarInstance.get(Calendar.DAY_OF_MONTH),
            calendarInstance.get(Calendar.MONTH),
            calendarInstance.get(Calendar.YEAR),
            notificationRepeatInterval,
            isNotificationRepeating,
            notificationColor,
            currentChannel,
            false
        )
    )

//    Toast.makeText(context, currentChannel.toString(), Toast.LENGTH_SHORT)
//        .show()
}

fun refreshReminders(reminders: List<Reminder>, context: Context) {
    if (reminders.isEmpty()) {
        return
    } else {
        for (reminder in reminders) {
            val currentChannel = reminder.notificationChannel
            val notificationRequestCode = reminder.requestCode
            val notificationTitle = reminder.title
            val notificationText = reminder.text
            val notificationMillis = reminder.timeInMillis
            val notificationRepeatInterval = reminder.repetition
            val isNotificationRepeating = reminder.isRepeating
            val notificationColor = reminder.color

            val intent = Intent(context, FancyTimeBroadcast::class.java)

            intent.putExtra(
                context.getString(R.string.notification_title_extra_name),
                notificationTitle
            )
            intent.putExtra(
                context.getString(R.string.notification_text_extra_name),
                notificationText
            )
            intent.putExtra(
                context.getString(R.string.notification_repeat_extra_name),
                isNotificationRepeating
            )
            intent.putExtra(
                context.getString(R.string.notification_repeat_interval_extra_name),
                notificationRepeatInterval
            )
            intent.putExtra(
                context.getString(R.string.notification_requestCode_extra_name),
                notificationRequestCode
            )
            intent.putExtra(
                context.getString(R.string.notification_time_extra_name),
                notificationMillis
            )
            intent.putExtra(
                context.getString(R.string.notification_channel_count_extra_name),
                currentChannel
            )

            intent.putExtra(context.getString(R.string.context_extra_name), notificationColor)

            val pendingIntent =
                PendingIntent.getBroadcast(
                    context,
                    notificationRequestCode,
                    intent,
                    0
                )

            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                notificationMillis,
                pendingIntent
            )
        }
    }
}
