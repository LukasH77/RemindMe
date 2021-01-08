package com.example.fancytimes

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.example.fancytimes.database.Reminder
import com.example.fancytimes.detail.DetailViewModel
import com.example.fancytimes.home.HomeViewModel
import com.example.fancytimes.setter.SetterViewModel
import java.util.*

fun hideSoftKeyboard(context: Context, view: View) {
    val inputMethodManager =
        context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
}

@RequiresApi(Build.VERSION_CODES.M)
fun handleAlarmsSetter(
    context: Context,
    viewModel: SetterViewModel,
    calendarInstance: Calendar,
    notificationMinute: Int,
    notificationHour: Int,
    notificationMillis: Long,
    preferences: SharedPreferences?,
    notificationTitle: String,
    notificationText: String,
    isNotificationRepeating: Boolean,
    notificationRepeatInterval: Long,
    color: Int
) {
    val notificationRequestCode =
        preferences!!.getInt(context.getString(R.string.request_code_key), 0)
    println(
        "Request code: $notificationRequestCode"
    )

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
    intent.putExtra(context.getString(R.string.context_extra_name), color)

    val pendingIntent =
        PendingIntent.getBroadcast(context, notificationRequestCode, intent, 0)

    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    alarmManager.setExactAndAllowWhileIdle(
        AlarmManager.RTC_WAKEUP,
        notificationMillis,
        pendingIntent
    )

    println(calendarInstance.get(Calendar.YEAR))

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
            color
        )
    )

    with(preferences.edit()) {
        this.putInt(context.getString(R.string.request_code_key), notificationRequestCode + 1)
        this.putInt(notificationRequestCode.toString(), notificationRequestCode)
        this.apply()
    }
    Toast.makeText(context, notificationRequestCode.toString(), Toast.LENGTH_SHORT)
        .show()
}

@RequiresApi(Build.VERSION_CODES.M)
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
    color: Int
) {
    val intent = Intent(context, FancyTimeBroadcast::class.java)

    println("IsRepeatingPassed $isNotificationRepeating")

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
    intent.putExtra(context.getString(R.string.context_extra_name), color)

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
            color
        )
    )
    Toast.makeText(context, notificationRequestCode.toString(), Toast.LENGTH_SHORT)
        .show()
}