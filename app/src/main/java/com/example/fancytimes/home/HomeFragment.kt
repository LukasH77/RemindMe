package com.example.fancytimes.home

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.text.format.DateFormat
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.TimePicker
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.example.fancytimes.FancyTimeBroadcast
import com.example.fancytimes.R
import com.example.fancytimes.databinding.FragmentHomeBinding
import java.util.*

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding

    private lateinit var timePicker: TimePicker

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        createNotificationChannel()
        setHasOptionsMenu(true)

        binding = DataBindingUtil.inflate(layoutInflater, R.layout.fragment_home, container, false)

        val timePicker = binding.tpTimePicker
        val addCustomTimeButton = binding.bAddCustomTime
        val notificationTitleField = binding.etNotificationTitle
        val notificationTextField = binding.etNotificationText

        val preferences = activity?.getSharedPreferences(
            getString(R.string.notification_preferences_key),
            Context.MODE_PRIVATE
        )

        if (preferences?.getInt(getString(R.string.request_code_key), 0) == null) {
//            println("Request code doesn't exist")
            with(preferences?.edit()) {
                this?.putInt(getString(R.string.request_code_key), 1)
                this?.apply()
            }
        }
//        println("Request code: ${preferences?.getInt(getString(R.string.request_code_key), 0)}")


        val system24hrs = DateFormat.is24HourFormat(requireContext())
        timePicker.setIs24HourView(system24hrs)

        addCustomTimeButton.setOnClickListener {
            val calendar = Calendar.getInstance()

            timePicker.hour = calendar.get(Calendar.HOUR_OF_DAY)
            timePicker.minute = calendar.get(Calendar.MINUTE)

            swapVisibility(false)
        }

        binding.bConfirmPick.setOnClickListener {
//            println("Request code: ${preferences?.getInt(getString(R.string.request_code_key), 0)}")
            val calendar = Calendar.getInstance()

            val notificationTitle =
                if (notificationTitleField.text.isBlank()) notificationTitleField.hint.toString() else notificationTitleField.text.toString()

            val notificationText =
                if (notificationTextField.text.isBlank()) notificationTitleField.hint.toString() else notificationTextField.text.toString()

            calendar.set(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH),
                timePicker.hour,
                timePicker.minute,
                0
            )

            val hourIsTooEarly = timePicker.hour < Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            val hourIsEqual = timePicker.hour == Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            val minuteIsTooEarly =
                hourIsEqual && timePicker.minute < Calendar.getInstance().get(Calendar.MINUTE)
            if (hourIsTooEarly || minuteIsTooEarly) {
//                println("Next day")
                calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH) + 1)
            }
            setAlarm(calendar.timeInMillis, preferences, notificationTitle, notificationText)
            swapVisibility(true)
            hideSoftKeyboard()
        }

        binding.ibExitTimePicker.setOnClickListener {
            swapVisibility(true)
            hideSoftKeyboard()
        }

        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun setAlarm(
        timeInMillis: Long,
        preferences: SharedPreferences?,
        notificationTitle: String,
        notificationText: String
    ) {
        val requestCode = preferences!!.getInt(getString(R.string.request_code_key), 0)

        val alarmManager = activity?.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(requireContext(), FancyTimeBroadcast::class.java)
        intent.putExtra(getString(R.string.notification_title_extra_name), notificationTitle)
        intent.putExtra(getString(R.string.notification_text_extra_name), notificationText)

        val pendingIntent = PendingIntent.getBroadcast(requireContext(), requestCode, intent, 0)

        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)

        with(preferences.edit()) {
            this.putInt(getString(R.string.request_code_key), requestCode + 1)
            this.apply()
        }
        Toast.makeText(requireContext(), "Reminder set.", Toast.LENGTH_SHORT).show()
    }

    private fun hideSoftKeyboard() {
        val inputMethodManager =
            activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(requireView().windowToken, 0)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                getString(R.string.notification_channel_id),
                "Notification Channel",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminders"
            }
            val notificationManager: NotificationManager =
                activity?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun swapVisibility(timePickerIsVisible: Boolean) {
        val mainLayout = arrayOf(
            binding.tvIntro,
//            binding.tvHowEarly,
            binding.bAddCustomTime,
            binding.bShowAndEdit,
//            binding.button2,
//            binding.button3,
            binding.bSwitchNotifications
        )

        val notificationCreationLayout = arrayOf(
            binding.tpTimePicker,
            binding.etNotificationTitle,
            binding.etNotificationText,
            binding.bConfirmPick,
            binding.ibExitTimePicker
        )

        if (timePickerIsVisible) {
            for (view in mainLayout) {
                view.visibility = View.VISIBLE
            }
            for (view in notificationCreationLayout) {
                view.visibility = View.GONE
            }
            binding.etNotificationTitle.text.clear()
            binding.etNotificationText.text.clear()
        } else {
            for (view in mainLayout) {
                view.visibility = View.GONE
            }
            for (view in notificationCreationLayout) {
                view.visibility = View.VISIBLE
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.overflow_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return NavigationUI.onNavDestinationSelected(
            item,
            requireView().findNavController()
        ) || super.onOptionsItemSelected(item)
    }
}