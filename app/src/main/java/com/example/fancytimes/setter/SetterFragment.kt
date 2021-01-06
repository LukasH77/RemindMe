package com.example.fancytimes.setter

import android.app.DatePickerDialog
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.text.format.DateFormat
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.CompoundButton
import android.widget.DatePicker
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.example.fancytimes.*
import com.example.fancytimes.database.ReminderDatabase
import com.example.fancytimes.databinding.FragmentSetterBinding
import java.util.*

class SetterFragment : Fragment() {
    private lateinit var binding: FragmentSetterBinding

    private lateinit var setterViewModelFactory: SetterViewModelFactory
    private lateinit var setterViewModel: SetterViewModel

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val reminderDao = ReminderDatabase.createInstance(requireContext()).reminderDao

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_setter, container, false)

        setterViewModelFactory = SetterViewModelFactory(reminderDao)
        setterViewModel =
            ViewModelProvider(this, setterViewModelFactory).get(SetterViewModel::class.java)

        val preferences = activity?.getSharedPreferences(
            getString(R.string.notification_preferences_key),
            Context.MODE_PRIVATE
        )

        val calendar = Calendar.getInstance()
//        val datePicker = DateSetter(preferences!!)



        val datePicker = DatePickerDialog(requireContext(), DatePickerDialog.OnDateSetListener { _: DatePicker?, year: Int, month: Int, day: Int ->
            println("date set")
            with(preferences!!.edit()) {
                this.putInt(requireContext().getString(R.string.day_key), day)
                this.putInt(requireContext().getString(R.string.month_key), month)
                this.putInt(requireContext().getString(R.string.year_key), year)
                this.apply()
            }
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))



        val timePicker = binding.tpTimePicker
        val notificationTitleField = binding.etNotificationTitle
        val notificationTextField = binding.etNotificationText
        val repeatingCheckBox = binding.cbRepeating
        val repeatingIntervalsSpinner = binding.sRepInterval

        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.repeatIntervals,
            android.R.layout.simple_spinner_item
        ).also {
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            repeatingIntervalsSpinner.adapter = it
        }

        repeatingIntervalsSpinner.onItemSelectedListener = IntervalSetter(preferences!!)

        repeatingCheckBox.setOnCheckedChangeListener { _: CompoundButton, checkedState: Boolean ->
            if (checkedState) repeatingIntervalsSpinner.visibility =
                View.VISIBLE else repeatingIntervalsSpinner.visibility = View.INVISIBLE
        }

        val system24hrs = DateFormat.is24HourFormat(requireContext())
        timePicker.setIs24HourView(system24hrs)

        with(preferences!!.edit()) {
            this.putInt(
                requireContext().getString(R.string.day_key),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            this.putInt(
                requireContext().getString(R.string.month_key),
                calendar.get(Calendar.MONTH)
            )
            this.putInt(requireContext().getString(R.string.year_key), calendar.get(Calendar.YEAR))
            this.apply()
        }

        timePicker.hour = calendar.get(Calendar.HOUR_OF_DAY)
        timePicker.minute = calendar.get(Calendar.MINUTE)

        binding.bEditDate.setOnClickListener {
            datePicker.show()
        }

        binding.bConfirmPick.setOnClickListener {
            val notificationTitle =
                if (notificationTitleField.text.isBlank()) notificationTitleField.hint.toString() else notificationTitleField.text.toString()

            val notificationText =
                if (notificationTextField.text.isBlank()) notificationTextField.hint.toString() else notificationTextField.text.toString()

            val isNotificationRepeating = repeatingCheckBox.isChecked

            val notificationRepeatInterval = if (isNotificationRepeating) preferences.getLong(
                getString(R.string.repeat_interval_key),
                0
            ) else 0

            calendar.set(
                preferences.getInt(getString(R.string.year_key), 0),
                preferences.getInt(getString(R.string.month_key), 0),
                preferences.getInt(getString(R.string.day_key), 0),
                timePicker.hour,
                timePicker.minute,
                0
            )


            val yearIsTooEarly =
                preferences.getInt(getString(R.string.year_key), 0) < Calendar.getInstance()
                    .get(Calendar.YEAR)

            val yearIsEqual =
                preferences.getInt(getString(R.string.year_key), 0) == Calendar.getInstance()
                    .get(Calendar.YEAR)

            val monthIsTooEarly = yearIsEqual &&
                    preferences.getInt(getString(R.string.month_key), 0) < Calendar.getInstance()
                .get(Calendar.MONTH)

            val monthIsEqual =
                preferences.getInt(getString(R.string.month_key), 0) == Calendar.getInstance()
                    .get(Calendar.MONTH)

            val dayIsTooEarly = monthIsEqual && preferences.getInt(
                getString(R.string.day_key),
                0
            ) < Calendar.getInstance().get(Calendar.DAY_OF_MONTH)

            val hourIsTooEarly = timePicker.hour < Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            val hourIsEqual = timePicker.hour == Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            val minuteIsTooEarly =
                hourIsEqual && timePicker.minute < Calendar.getInstance().get(Calendar.MINUTE)

            if (yearIsTooEarly || monthIsTooEarly || dayIsTooEarly) {
                Toast.makeText(requireContext(), "Invalid date!", Toast.LENGTH_SHORT).show()
                println("Invalid date!")
                return@setOnClickListener
            }

            if (hourIsTooEarly || minuteIsTooEarly) {
                calendar.set(
                    Calendar.DAY_OF_MONTH,
                    preferences.getInt(getString(R.string.day_key), 0) + 1
                )
                println(preferences.getInt(getString(R.string.day_key), 0))
                Toast.makeText(requireContext(), "Triggered", Toast.LENGTH_SHORT).show()
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
                isNotificationRepeating,
                notificationRepeatInterval
            )

            hideSoftKeyboard(requireContext(), requireView())

            it.findNavController()
                .navigate(SetterFragmentDirections.actionSetterFragmentToHomeFragment())
        }

        return binding.root
    }
}