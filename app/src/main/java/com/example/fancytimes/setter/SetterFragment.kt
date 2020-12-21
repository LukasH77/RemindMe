package com.example.fancytimes.setter

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.text.format.DateFormat
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.example.fancytimes.R
import com.example.fancytimes.database.ReminderDatabase
import com.example.fancytimes.databinding.FragmentSetterBinding
import com.example.fancytimes.handleAlarmsSetter
import com.example.fancytimes.hideSoftKeyboard
import java.util.*

class SetterFragment : Fragment() {
    private lateinit var binding: FragmentSetterBinding

    private lateinit var setterViewModelFactory: SetterViewModelFactory
    private lateinit var setterViewModel: SetterViewModel

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val reminderDao = ReminderDatabase.createInstance(requireContext()).reminderDao

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_setter, container, false)

        setterViewModelFactory = SetterViewModelFactory(reminderDao)
        setterViewModel = ViewModelProvider(this, setterViewModelFactory).get(SetterViewModel::class.java)

        val preferences = activity?.getSharedPreferences(
            getString(R.string.notification_preferences_key),
            Context.MODE_PRIVATE
        )

        val timePicker = binding.tpTimePicker
        val notificationTitleField = binding.etNotificationTitle
        val notificationTextField = binding.etNotificationText
        val repeatingCheckBox = binding.cbRepeating

        val system24hrs = DateFormat.is24HourFormat(requireContext())
        timePicker.setIs24HourView(system24hrs)

        val calendar = Calendar.getInstance()

        timePicker.hour = calendar.get(Calendar.HOUR_OF_DAY)
        timePicker.minute = calendar.get(Calendar.MINUTE)

        binding.bConfirmPick.setOnClickListener {
            val notificationTitle =
                if (notificationTitleField.text.isBlank()) notificationTitleField.hint.toString() else notificationTitleField.text.toString()

            val notificationText =
                if (notificationTextField.text.isBlank()) notificationTextField.hint.toString() else notificationTextField.text.toString()

            val isNotificationRepeating = repeatingCheckBox.isChecked

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
                calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH) + 1)
//                Toast.makeText(requireContext(), "Triggered", Toast.LENGTH_SHORT).show()
            }
            handleAlarmsSetter(
                requireContext(),
                setterViewModel,
                calendar,
                timePicker.minute,
                timePicker.hour,
                calendar.timeInMillis,
                preferences,
                notificationTitle,
                notificationText,
                isNotificationRepeating
            )

            hideSoftKeyboard(requireContext(), requireView())

            it.findNavController().navigate(SetterFragmentDirections.actionSetterFragmentToHomeFragment())
        }

        return binding.root
    }
}