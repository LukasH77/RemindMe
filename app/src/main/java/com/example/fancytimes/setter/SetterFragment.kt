package com.example.fancytimes.setter

import android.app.DatePickerDialog
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.text.format.DateFormat
import android.view.KeyEvent
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.fancytimes.*
import com.example.fancytimes.database.ReminderDatabase
import com.example.fancytimes.databinding.FragmentSetterBinding
import com.jaredrummler.android.colorpicker.ColorPickerDialog
import java.lang.Exception
import java.util.*

class SetterFragment : Fragment() {
    private lateinit var binding: FragmentSetterBinding

    private lateinit var setterViewModelFactory: SetterViewModelFactory
    private lateinit var setterViewModel: SetterViewModel

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
//        calendar.timeInMillis = calendar.timeInMillis + 1000 * 60 * 5

        val timePicker = binding.tpTimePicker
        val notificationTitleField = binding.etNotificationTitle
        val notificationTextField = binding.etNotificationText
        val repeatingCheckBox = binding.cbRepeating
        val repeatingIntervalsSpinner = binding.sRepInterval

        val system24hrs = DateFormat.is24HourFormat(requireContext())

        with(preferences!!.edit())
        {
            this.putInt(
                requireContext().getString(R.string.day_key),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            this.putInt(
                requireContext().getString(R.string.month_key),
                calendar.get(Calendar.MONTH)
            )
            this.putInt(
                requireContext().getString(R.string.year_key),
                calendar.get(Calendar.YEAR)
            )
            if (preferences.getInt(requireContext().getString(R.string.color_key_setter), -1) == -1) {
                this.putInt(
                    requireContext().getString(R.string.color_key_setter),
                    0xfff2f2f2.toInt()
                )
            }  // put in grey if not set yet
            this.apply()
        }

        var setDay = preferences.getInt(getString(R.string.day_key), 0)
        var setMonth = preferences.getInt(getString(R.string.month_key), 0)
        var setYear = preferences.getInt(getString(R.string.year_key), 0)

        binding.tvSetDate.text =
            "${if (setDay < 10) "0$setDay" else setDay}.${if (setMonth < 9) "0${setMonth + 1}" else setMonth + 1}.$setYear"

        val datePicker = DatePickerDialog(
            requireContext(),
            { datePicker: DatePicker?, year: Int, month: Int, day: Int ->
//                println("date set")
                with(preferences.edit()) {
                    this.putInt(requireContext().getString(R.string.day_key), day)
                    this.putInt(requireContext().getString(R.string.month_key), month)
                    this.putInt(requireContext().getString(R.string.year_key), year)
                    this.apply()
                }





                setDay = preferences.getInt(getString(R.string.day_key), 0)
                setMonth = preferences.getInt(getString(R.string.month_key), 0)
                setYear = preferences.getInt(getString(R.string.year_key), 0)

                //check too early here as well in case you set a hour/minute too early then put the date on today - make the dateText update!
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
                    (hourIsTooEarly || hourIsEqual) && timePicker.minute <= Calendar.getInstance()
                        .get(Calendar.MINUTE)


                if (hourIsTooEarly || minuteIsTooEarly) {
                    binding.tvSetDate.text =
                        "${if (setDay < 9) "0${setDay + 1}" else setDay + 1}.${if (setMonth < 9) "0${setMonth + 1}" else setMonth + 1}.$setYear"
                    datePicker!!.updateDate(setYear, setMonth, setDay + 1)
                } else {
                    binding.tvSetDate.text =
                        "${if (setDay < 10) "0$setDay" else setDay}.${if (setMonth < 9) "0${setMonth + 1}" else setMonth + 1}.$setYear"
                }
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePicker.datePicker.minDate = System.currentTimeMillis()
        datePicker.datePicker.firstDayOfWeek = Calendar.MONDAY


        val colorPicker =
            ColorPickerDialog.newBuilder().setDialogType(ColorPickerDialog.TYPE_PRESETS)
                .setPresets(
                    intArrayOf(
                        0xfff9f3ff.toInt(),
                        0xfff3f4ff.toInt(),
                        0xfff3fcff.toInt(),
                        0xfff3fff3.toInt(),
                        0xfffefff3.toInt(),
                        0xfffff3f3.toInt(),
                        0xfff2f2f2.toInt(),
                        0xffffffff.toInt()
                    )
                )
                .setAllowCustom(false).setShowColorShades(false)

        binding.tvColorPreview.setBackgroundColor(
            preferences.getInt(
                requireContext().getString(R.string.color_key_setter),
                0
            )
        )

        timePicker.setIs24HourView(system24hrs)

        timePicker.hour = calendar.get(Calendar.HOUR_OF_DAY)
        timePicker.minute = calendar.get(Calendar.MINUTE)

        timePicker.setOnTimeChangedListener { _: TimePicker, _: Int, _: Int ->

            hideSoftKeyboard(requireContext(), requireView())

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
                (hourIsTooEarly || hourIsEqual) && timePicker.minute <= Calendar.getInstance()
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

        repeatingIntervalsSpinner.onItemSelectedListener = IntervalSetter(preferences)

        repeatingIntervalsSpinner.setSelection(4)

        repeatingCheckBox.isChecked =
            preferences.getBoolean(getString(R.string.repeat_preference_key), true)
        repeatingIntervalsSpinner.visibility =
            if (repeatingCheckBox.isChecked) View.VISIBLE else View.INVISIBLE

        repeatingCheckBox.setOnCheckedChangeListener { _: CompoundButton, checkedState: Boolean ->
            if (checkedState) repeatingIntervalsSpinner.visibility =
                View.VISIBLE else repeatingIntervalsSpinner.visibility = View.INVISIBLE
            with(preferences.edit()) {
                this.putBoolean(getString(R.string.repeat_preference_key), checkedState)
                this.apply()
            }
        }

        val temp = arrayOf(
            binding.tvColorPreview,
            binding.cbRepeating,
            binding.tvSetDate,
            binding.tpTimePicker,
            binding.clMainLayout
        )

        for (i in temp) {
            i.setOnClickListener {
                hideSoftKeyboard(requireContext(), requireView())
            }
        }

        binding.ibEditDate.setOnClickListener {
            println("edit date")
            datePicker.show()
            hideSoftKeyboard(requireContext(), requireView())
        }

        binding.ibEditColor.setOnClickListener {  //main activity cpdListener handles color selection
            colorPicker.setColor(
                preferences.getInt(
                    requireContext().getString(R.string.color_key_setter),
                    0
                )
            )
            colorPicker.show(requireActivity())
            hideSoftKeyboard(requireContext(), requireView())
        }

        binding.ibConfirmPick.setOnClickListener {
            println("confirmed")
            val notificationTitle =
                if (notificationTitleField.text.isBlank()) getString(R.string.reminder) else notificationTitleField.text.toString()

            val notificationText = notificationTextField.text.toString()

            val isNotificationRepeating = repeatingCheckBox.isChecked

            val notificationRepeatInterval = if (isNotificationRepeating) preferences.getLong(
                getString(R.string.repeat_interval_key),
                0
            ) else 0

            val color = preferences.getInt(requireContext().getString(R.string.color_key_setter), 0)

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
                (hourIsTooEarly || hourIsEqual) && timePicker.minute <= Calendar.getInstance()
                    .get(Calendar.MINUTE)

            if (yearIsTooEarly || monthIsTooEarly || dayIsTooEarly) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.invalid_date),
                    Toast.LENGTH_LONG
                ).show()
//                println("Invalid date!")
                return@setOnClickListener
            }

            if (hourIsTooEarly || minuteIsTooEarly) {
                calendar.set(
                    Calendar.DAY_OF_MONTH,
                    preferences.getInt(getString(R.string.day_key), 0) + 1
                )
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

            //calculating the exact difference in days, hours, minutes from now until the set reminder
            //did this "top down", first calculating the days, then the remaining hours and minutes. you could do this the other way around it just came to me like this
            val diffInDaysExact = (calendar.timeInMillis - Calendar.getInstance().timeInMillis).toDouble() / 1000 / 60 / 60 / 24
            val diffInDays = diffInDaysExact.toInt()
            val hoursRestExact = diffInDaysExact % 1 * 24
            val hoursRest = hoursRestExact.toInt()
            val minutesRest = (hoursRestExact % 1 * 60).toInt()

            //this is just deciding on a fitting string to tell the user how long it is until the reminder will go off, don't think about it too much
            when {
                diffInDays >= 31 -> Toast.makeText(requireContext(), "I'll remind you in more than a month.", Toast.LENGTH_LONG).show()
                diffInDays >= 365 -> Toast.makeText(requireContext(), "I'll remind you in more than a year.", Toast.LENGTH_LONG).show()
                diffInDays == 0 && hoursRest == 0 && minutesRest == 0 -> Toast.makeText(requireContext(), "I'll remind you in less than a minute.", Toast.LENGTH_LONG).show()
                diffInDays == 0 && hoursRest == 0 -> Toast.makeText(requireContext(), "I'll remind you in $minutesRest ${if (minutesRest > 1) "minutes" else "minute"}.", Toast.LENGTH_LONG).show()
                diffInDays == 0 && minutesRest == 0 -> Toast.makeText(requireContext(), "I'll remind you in $hoursRest ${if (hoursRest > 1) "hours" else "hour"}.", Toast.LENGTH_LONG).show()
                diffInDays == 0 -> Toast.makeText(requireContext(), "I'll remind you in $hoursRest ${if (hoursRest > 1) "hours" else "hour"} and $minutesRest ${if (minutesRest > 1) "minutes" else "minute"}.", Toast.LENGTH_LONG).show()
                hoursRest == 0 && minutesRest == 0 -> Toast.makeText(requireContext(), "I'll remind you in $diffInDays ${if (diffInDays > 1) "days" else "day"}", Toast.LENGTH_LONG).show()
                hoursRest == 0 -> Toast.makeText(requireContext(), "I'll remind you in $diffInDays ${if (diffInDays > 1) "days" else "day"} and $minutesRest ${if (minutesRest > 1) "minutes" else "minute"}.", Toast.LENGTH_LONG).show()
                minutesRest == 0 -> Toast.makeText(requireContext(), "I'll remind you in $diffInDays ${if (diffInDays > 1) "days" else "day"} and $hoursRest ${if (hoursRest > 1) "hours" else "hour"}.", Toast.LENGTH_LONG).show()
                else -> Toast.makeText(requireContext(), "I'll remind you in $diffInDays ${if (diffInDays > 1) "days" else "day"}, $hoursRest ${if (hoursRest > 1) "hours" else "hour"} and $minutesRest ${if (minutesRest > 1) "minutes" else "minute"}.", Toast.LENGTH_LONG).show()
            }

            hideSoftKeyboard(requireContext(), requireView())

            requireActivity().onBackPressed()
        }

        return binding.root
    }

    fun setterKeyUp(keyCode: Int, event: KeyEvent?) {
        println("setterKeyDown called")
        if (keyCode == KeyEvent.KEYCODE_NAVIGATE_NEXT) {
            println("next key pressed setter")
        }
    }
}