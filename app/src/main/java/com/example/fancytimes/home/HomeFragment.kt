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
import kotlin.math.min

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding

    private lateinit var timePicker: TimePicker

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        createNotificationChannel()

        binding = DataBindingUtil.inflate(layoutInflater, R.layout.fragment_home, container, false)
        setHasOptionsMenu(true)

        val timePicker = binding.tpTimePicker
        val addCustomTimeButton = binding.bAddCustomTime
        val standardLayout = arrayOf(
            binding.tvIntro,
//            binding.tvHowEarly,
            addCustomTimeButton,
            binding.bShowAndEdit,
//            binding.button2,
//            binding.button3,
            binding.bSwitchNotifications
        )

        val preferences =  activity?.getSharedPreferences(getString(R.string.notification_preferences_key), Context.MODE_PRIVATE)

        if (preferences?.getInt(getString(R.string.request_code_key), 0) == null) {
            println("Request code doesn't exist")
            with(preferences?.edit()) {
                this?.putInt(getString(R.string.request_code_key), 1)
                this?.apply()
            }
        }
        println("Request code: ${preferences?.getInt(getString(R.string.request_code_key), 0)}")


        val system24hrs = DateFormat.is24HourFormat(requireContext())
        timePicker.setIs24HourView(system24hrs)

        addCustomTimeButton.setOnClickListener {
            val calendar = Calendar.getInstance()

            timePicker.hour = calendar.get(Calendar.HOUR_OF_DAY)
            timePicker.minute = calendar.get(Calendar.MINUTE)

            for (view in standardLayout) {
                view.visibility = View.GONE
            }
            timePicker.visibility = View.VISIBLE
            binding.bConfirmPick.visibility = View.VISIBLE
            binding.ibExitTimePicker.visibility = View.VISIBLE
        }

        binding.bConfirmPick.setOnClickListener {
            println("Request code: ${preferences?.getInt(getString(R.string.request_code_key), 0)}")
            val calendar = Calendar.getInstance()
            for (view in standardLayout) {
                view.visibility = View.VISIBLE
            }

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
            val minuteIsTooEarly = hourIsEqual && timePicker.minute < Calendar.getInstance().get(Calendar.MINUTE)
            if (hourIsTooEarly || minuteIsTooEarly) {
                    Toast.makeText(requireContext(), "Invalid time...for now.", Toast.LENGTH_SHORT).show()
                    timePicker.visibility = View.GONE
                    binding.bConfirmPick.visibility = View.GONE
                    binding.ibExitTimePicker.visibility = View.GONE
                    return@setOnClickListener
            }

            Toast.makeText(requireContext(), "Time set.", Toast.LENGTH_SHORT).show()
            setAlarm(calendar.timeInMillis, preferences)

            timePicker.visibility = View.GONE
            binding.bConfirmPick.visibility = View.GONE
            binding.ibExitTimePicker.visibility = View.GONE
            hideSoftKeyboard()
        }

        binding.ibExitTimePicker.setOnClickListener {
            for (view in standardLayout) {
                view.visibility = View.VISIBLE
            }
            timePicker.visibility = View.GONE
            binding.bConfirmPick.visibility = View.GONE
            binding.ibExitTimePicker.visibility = View.GONE
            hideSoftKeyboard()
        }

        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun setAlarm(timeInMillis: Long, preferences: SharedPreferences?) {
        val requestCode = preferences!!.getInt(getString(R.string.request_code_key), 0)

        val alarmManager = activity?.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(requireContext(), FancyTimeBroadcast::class.java)

        val pendingIntent = PendingIntent.getBroadcast(requireContext(), requestCode, intent, 0)

        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)

        with(preferences.edit()) {
            this.putInt(getString(R.string.request_code_key), requestCode + 1)
            this.apply()
        }
    }

    private fun hideSoftKeyboard() {
        val inputMethodManager = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(requireView().windowToken, 0)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Notification Channel"
            val text = "Fancy Time notifications"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("FancyTimes notifications", name, importance).apply {
                description = text
            }
            val notificationManager: NotificationManager =
                activity?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
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