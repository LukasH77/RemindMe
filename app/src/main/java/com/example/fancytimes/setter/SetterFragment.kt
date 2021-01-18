package com.example.fancytimes.setter

import android.app.DatePickerDialog
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.text.format.DateFormat
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.core.graphics.toColorInt
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.example.fancytimes.*
import com.example.fancytimes.database.ReminderDatabase
import com.example.fancytimes.databinding.FragmentSetterBinding
import com.jaredrummler.android.colorpicker.ColorPickerDialog
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener
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
            this.putInt(requireContext().getString(R.string.color_key), 0xffcfd8dc.toInt())
            this.apply()
        }

        var setDay = preferences.getInt(getString(R.string.day_key), 0)
        var setMonth = preferences.getInt(getString(R.string.month_key), 0)
        var setYear = preferences.getInt(getString(R.string.year_key), 0)

        binding.tvSetDate.text =
            "${if (setDay < 10) "0$setDay" else setDay}.${if (setMonth < 9) "0${setMonth + 1}" else setMonth + 1}.$setYear"

        val datePicker = DatePickerDialog(
            requireContext(),
            { _: DatePicker?, year: Int, month: Int, day: Int ->
                println("date set")
                with(preferences!!.edit()) {
                    this.putInt(requireContext().getString(R.string.day_key), day)
                    this.putInt(requireContext().getString(R.string.month_key), month)
                    this.putInt(requireContext().getString(R.string.year_key), year)
                    this.apply()
                }

                setDay = preferences.getInt(getString(R.string.day_key), 0)
                setMonth = preferences.getInt(getString(R.string.month_key), 0)
                setYear = preferences.getInt(getString(R.string.year_key), 0)

                binding.tvSetDate.text =
                    "${if (setDay < 10) "0$setDay" else setDay}.${if (setMonth < 9) "0${setMonth + 1}" else setMonth + 1}.$setYear"
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        val colorPicker =
            ColorPickerDialog.newBuilder().setDialogType(ColorPickerDialog.TYPE_PRESETS)
                .setPresets(
                    intArrayOf(
                        0xffeccced.toInt(),
                        0xffd0cced.toInt(),
                        0xffccdbed.toInt(),
                        0xffccede6.toInt(),
                        0xffccedd0.toInt(),
                        0xffedeccc.toInt(),
                        0xffeddccc.toInt(),
                        0xffedcccc.toInt(),
                        0xffcfd8dc.toInt(),
                        0xffffffff.toInt()
                    )
                )
                .setAllowCustom(false).setShowColorShades(false)

        binding.tvColorPreview.setBackgroundColor(
            preferences.getInt(
                requireContext().getString(R.string.color_key),
                0
            )
        )
        colorPicker.setColor(preferences.getInt(requireContext().getString(R.string.color_key), 0))

        val timePicker = binding.tpTimePicker
        val notificationTitleField = binding.etNotificationTitle
        val notificationTextField = binding.etNotificationText
        val repeatingCheckBox = binding.cbRepeating
        val repeatingIntervalsSpinner = binding.sRepInterval

        timePicker.setOnTimeChangedListener { _: TimePicker, _: Int, _: Int ->
            val yearIsTooEarly =
                preferences.getInt(getString(R.string.year_key), 0) < Calendar.getInstance()
                    .get(Calendar.YEAR)

            val yearIsEqual =
                preferences.getInt(getString(R.string.year_key), 0) == Calendar.getInstance()
                    .get(Calendar.YEAR)

            val monthIsTooEarly = (yearIsTooEarly || yearIsEqual) &&
                    preferences.getInt(getString(R.string.month_key), 0) < Calendar.getInstance()
                .get(Calendar.MONTH)

            val monthIsEqual = yearIsEqual &&
                    preferences.getInt(getString(R.string.month_key), 0) == Calendar.getInstance()
                .get(Calendar.MONTH)

            val dayIsTooEarly = (monthIsTooEarly || monthIsEqual) && preferences.getInt(
                getString(R.string.day_key),
                0
            ) < Calendar.getInstance().get(Calendar.DAY_OF_MONTH)

            val dayIsEqual = monthIsEqual &&
                    preferences.getInt(getString(R.string.day_key), 0) == Calendar.getInstance()
                .get(Calendar.DAY_OF_MONTH)

            val hourIsTooEarly =
                (dayIsEqual || dayIsTooEarly) && timePicker.hour < Calendar.getInstance()
                    .get(Calendar.HOUR_OF_DAY)
            val hourIsEqual =
                dayIsEqual && timePicker.hour == Calendar.getInstance().get(Calendar.HOUR_OF_DAY)

            val minuteIsTooEarly =
                (hourIsTooEarly || hourIsEqual) && timePicker.minute < Calendar.getInstance()
                    .get(Calendar.MINUTE)

            if (dayIsTooEarly || monthIsTooEarly || yearIsTooEarly) {
                return@setOnTimeChangedListener
            } else if (hourIsTooEarly || minuteIsTooEarly) {
                binding.tvSetDate.text =
                    "${if (setDay < 9) "0${setDay + 1}" else setDay + 1}.${if (setMonth < 9) "0${setMonth + 1}" else setMonth + 1}.$setYear"
                datePicker.updateDate(setYear, setMonth, setDay + 1)
            } else {
                binding.tvSetDate.text =
                    "${if (setDay < 10) "0$setDay" else setDay}.${if (setMonth < 9) "0${setMonth + 1}" else setMonth + 1}.$setYear"
                datePicker.updateDate(setYear, setMonth, setDay)
            }
        }

        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.repeatIntervals,
            android.R.layout.simple_spinner_item
        ).also {
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            repeatingIntervalsSpinner.adapter = it
        }

        repeatingIntervalsSpinner.onItemSelectedListener = IntervalSetter(preferences!!)

        repeatingIntervalsSpinner.setSelection(4)

        repeatingCheckBox.setOnCheckedChangeListener { _: CompoundButton, checkedState: Boolean ->
            if (checkedState) repeatingIntervalsSpinner.visibility =
                View.VISIBLE else repeatingIntervalsSpinner.visibility = View.INVISIBLE
        }

        val system24hrs = DateFormat.is24HourFormat(requireContext())
        timePicker.setIs24HourView(system24hrs)

        timePicker.hour = calendar.get(Calendar.HOUR_OF_DAY)
        timePicker.minute = calendar.get(Calendar.MINUTE)

        binding.ibEditDate.setOnClickListener {
            datePicker.show()
        }

        binding.ibEditColor.setOnClickListener {
            colorPicker.setColor(preferences.getInt(requireContext().getString(R.string.color_key), 0))
            colorPicker.show(requireActivity())
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

            val color = preferences.getInt(requireContext().getString(R.string.color_key), 0)

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

            val monthIsTooEarly = (yearIsTooEarly || yearIsEqual) &&
                    preferences.getInt(getString(R.string.month_key), 0) < Calendar.getInstance()
                .get(Calendar.MONTH)

            val monthIsEqual = yearIsEqual &&
                    preferences.getInt(getString(R.string.month_key), 0) == Calendar.getInstance()
                .get(Calendar.MONTH)

            val dayIsTooEarly = (monthIsTooEarly || monthIsEqual) && preferences.getInt(
                getString(R.string.day_key),
                0
            ) < Calendar.getInstance().get(Calendar.DAY_OF_MONTH)

            val dayIsEqual = monthIsEqual &&
                    preferences.getInt(getString(R.string.day_key), 0) == Calendar.getInstance()
                .get(Calendar.DAY_OF_MONTH)

            val hourIsTooEarly =
                (dayIsEqual || dayIsTooEarly) && timePicker.hour < Calendar.getInstance()
                    .get(Calendar.HOUR_OF_DAY)
            val hourIsEqual =
                dayIsEqual && timePicker.hour == Calendar.getInstance().get(Calendar.HOUR_OF_DAY)

            val minuteIsTooEarly =
                (hourIsTooEarly || hourIsEqual) && timePicker.minute < Calendar.getInstance()
                    .get(Calendar.MINUTE)

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
                notificationRepeatInterval,
                color
            )

            hideSoftKeyboard(requireContext(), requireView())

            requireActivity().onBackPressed()

//            it.findNavController()
//                .navigate(SetterFragmentDirections.actionSetterFragmentToHomeFragment())
        }

        return binding.root
    }
}