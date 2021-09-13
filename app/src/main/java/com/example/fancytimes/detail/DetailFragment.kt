package com.example.fancytimes.detail

import android.app.AlarmManager
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.format.DateFormat
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.*
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import com.example.fancytimes.*
import com.example.fancytimes.database.ReminderDatabase
import com.example.fancytimes.databinding.FragmentDetailBinding
import com.jaredrummler.android.colorpicker.ColorPickerDialog
import java.util.*

class DetailFragment : Fragment() {
    private lateinit var binding: FragmentDetailBinding
    private lateinit var detailViewModel: DetailViewModel

    private lateinit var notificationTitle: String
    private lateinit var notificationText: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val receivedArgs by navArgs<DetailFragmentArgs>()
        val requestCode = receivedArgs.requestCode

        val reminderDao = ReminderDatabase.createInstance(requireContext()).reminderDao
        val detailViewModelFactory = DetailViewModelFactory(reminderDao, requestCode)

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_detail, container, false)
        detailViewModel =
            ViewModelProvider(this, detailViewModelFactory).get(DetailViewModel::class.java)

        val preferences = activity?.getSharedPreferences(
            getString(R.string.notification_preferences_key),
            Context.MODE_PRIVATE
        )

        val calendar = Calendar.getInstance()
//        val datePicker = DateSetter(preferences!!)

        val timePicker = binding.tpTimePicker
        val title = binding.etNotificationTitle
        val text = binding.etNotificationText
        val repeatingCheckBox = binding.cbRepeating
        val repeatingIntervalsSpinner = binding.sRepInterval

        val system24hrs = DateFormat.is24HourFormat(requireContext())

        var currentChannel = 0
        var setDay = 0
        var setMonth = 0
        var setYear = 0

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

        val datePicker = DatePickerDialog(
            requireContext(),
            { datePicker: DatePicker?, year: Int, month: Int, day: Int ->
//                println("date set")

                with(preferences!!.edit()) {
                    this.putInt(requireContext().getString(R.string.day_key), day)
                    this.putInt(requireContext().getString(R.string.month_key), month)
                    this.putInt(requireContext().getString(R.string.year_key), year)
                    this.apply()
                }

                setDay = preferences.getInt(getString(R.string.day_key), 0)
                setMonth = preferences.getInt(getString(R.string.month_key), 0)
                setYear = preferences.getInt(getString(R.string.year_key), 0)

                val yearIsTooEarly =
                    preferences.getInt(getString(R.string.year_key), 0) < Calendar.getInstance()
                        .get(Calendar.YEAR)

                val yearIsEqual =
                    preferences.getInt(getString(R.string.year_key), 0) == Calendar.getInstance()
                        .get(Calendar.YEAR)

                val monthIsTooEarly = (yearIsTooEarly || yearIsEqual) &&
                        preferences.getInt(
                            getString(R.string.month_key),
                            0
                        ) < Calendar.getInstance()
                    .get(Calendar.MONTH)

                val monthIsEqual = yearIsEqual &&
                        preferences.getInt(
                            getString(R.string.month_key),
                            0
                        ) == Calendar.getInstance()
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
                    dayIsEqual && timePicker.hour == Calendar.getInstance()
                        .get(Calendar.HOUR_OF_DAY)

                val minuteIsTooEarly =
                    (hourIsTooEarly || hourIsEqual) && timePicker.minute <= Calendar.getInstance()
                        .get(Calendar.MINUTE)

                if (hourIsTooEarly || minuteIsTooEarly) {
                    binding.tvSetDate.text =
                        "${if (setDay < 9) "0${setDay + 1}" else setDay + 1}.${if (setMonth < 9) "0${setMonth + 1}" else setMonth + 1}.$setYear"
//                                "($currentChannel)" +
//                                "1"
                    datePicker!!.updateDate(setYear, setMonth, setDay + 1)
                } else {
                    binding.tvSetDate.text =
                        "${if (setDay < 10) "0$setDay" else setDay}.${if (setMonth < 9) "0${setMonth + 1}" else setMonth + 1}.$setYear"
//                                "($currentChannel)" +
//                                "2"
                }
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePicker.datePicker.minDate = System.currentTimeMillis()
        datePicker.datePicker.firstDayOfWeek = Calendar.MONDAY



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
            if (checkedState) {
                repeatingIntervalsSpinner.setSelection(3)
                repeatingIntervalsSpinner.visibility = View.VISIBLE
            } else repeatingIntervalsSpinner.visibility = View.INVISIBLE
        }


        timePicker.setIs24HourView(system24hrs)

        binding.ibEditDate.setOnClickListener {
            datePicker.show()
            hideSoftKeyboard(requireContext(), requireView())
        }

        // this variable stops the on timeChanged listener to react to the initial time setting
        var isTimeInitialized = false
        detailViewModel.selectedReminder.observe(viewLifecycleOwner, {
            it?.let {
                currentChannel = it.notificationChannel
//                println("Detail current channel: $currentChannel")

                val color = it.color
                colorPicker.setColor(color)
                binding.tvColorPreview.setBackgroundColor(color)

                setDay = it.day
                setMonth = it.month
                setYear = it.year

                binding.tvSetDate.text =
                    "${if (it.day < 10) "0${it.day}" else it.day}.${if (it.month < 9) "0${it.month + 1}" else it.month + 1}.${it.year}  "
//                            "(${it.notificationChannel})" +
//                            "3"
                binding.tpTimePicker.minute = it.minute
                binding.tpTimePicker.hour = it.hour
                title.text = SpannableStringBuilder(it.title)
                text.text = SpannableStringBuilder(it.text)
                datePicker.updateDate(it.year, it.month, it.day)

//                println("it.repetition: ${it.repetition}")

                val oneSecondInMillis = 1000L
                val oneMinuteInMillis = oneSecondInMillis * 60
                val oneHourInMillis = oneMinuteInMillis * 60
                val oneDayInMillis = oneHourInMillis * 24
                val oneWeekInMillis = oneDayInMillis * 7
                if (it.repetition != 0L) {
                    binding.cbRepeating.isChecked = true
                    when (it.repetition) {
                        // oneMinuteInMillis * 5 -> repeatingIntervalsSpinner.setSelection(0)
                        oneMinuteInMillis * 15 -> repeatingIntervalsSpinner.setSelection(0)
                        oneMinuteInMillis * 30 -> repeatingIntervalsSpinner.setSelection(1)
                        oneHourInMillis -> repeatingIntervalsSpinner.setSelection(2)
                        oneDayInMillis -> repeatingIntervalsSpinner.setSelection(3)
                        oneDayInMillis * 2 -> repeatingIntervalsSpinner.setSelection(4)
                        oneWeekInMillis -> repeatingIntervalsSpinner.setSelection(5)
                        oneWeekInMillis * 2 -> repeatingIntervalsSpinner.setSelection(6)
                        1L -> repeatingIntervalsSpinner.setSelection(7)
                        2L -> repeatingIntervalsSpinner.setSelection(8)
                    }
                }

                with(preferences.edit()) {
                    this.putInt(getString(R.string.color_key_detail), color)
                    this.putInt(requireContext().getString(R.string.day_key), setDay)
                    this.putInt(requireContext().getString(R.string.month_key), setMonth)
                    this.putInt(requireContext().getString(R.string.year_key), setYear)
                    if (it.repetition != 0L) {
                        this.putLong(
                            requireContext().getString(R.string.repeat_interval_key),
                            it.repetition!!
                        )
                    }
                    this.apply()
                }

                isTimeInitialized = true
            }
        })

        timePicker.setOnTimeChangedListener { _: TimePicker, _: Int, _: Int ->
            hideSoftKeyboard(requireContext(), requireView())
            if (!isTimeInitialized) return@setOnTimeChangedListener

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
//                            "($currentChannel)" +
//                            "4"
                datePicker.updateDate(setYear, setMonth, setDay + 1)
            } else {
                binding.tvSetDate.text =
                    "${if (setDay < 10) "0$setDay" else setDay}.${if (setMonth < 9) "0${setMonth + 1}" else setMonth + 1}.$setYear"
//                            "($currentChannel)" +
//                            "5"
                datePicker.updateDate(setYear, setMonth, setDay)
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

        binding.ibEditColor.setOnClickListener {
            colorPicker.setColor(
                preferences.getInt(
                    requireContext().getString(R.string.color_key_detail),
                    0
                )
            )
            colorPicker.show(requireActivity())
            hideSoftKeyboard(requireContext(), requireView())
        }

        binding.ibDeleteDetail.setOnClickListener {
            val alarmManager = it.context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            AlertDialog.Builder(it.context)
                .setTitle(requireContext().getString(R.string.cancel_specific_reminder_title))
                .setMessage(requireContext().getString(R.string.cancel_confirmation_specific))
                .setPositiveButton(requireContext().getString(R.string.yes)) { _: DialogInterface, _: Int ->
                    val intent = Intent(it.context, FancyTimeBroadcast::class.java)
                    try {
                        alarmManager.cancel(
                            PendingIntent.getBroadcast(
                                it.context,
                                requestCode,
                                intent,
                                PendingIntent.FLAG_NO_CREATE
                            )
                        )
                        with(preferences.edit()) {
                            this.remove(requestCode.toString())
                            this.apply()
                        }
                    } catch (e: Exception) {
//                        println("cancel() called with a null PendingIntent")
                    } finally {
                        detailViewModel.deleteByRequestCode(requestCode)
                        requireActivity().onBackPressed()
                    }
                }
                .setNegativeButton(requireContext().getString(R.string.no), null)
                .setIcon(android.R.drawable.ic_dialog_alert).show()
        }

        binding.ibConfirmPick.setOnClickListener {
            calendar.set(
                preferences.getInt(getString(R.string.year_key), 0),
                preferences.getInt(getString(R.string.month_key), 0),
                preferences.getInt(getString(R.string.day_key), 0),
                timePicker.hour,
                timePicker.minute,
                0
            )

            val color = preferences.getInt(requireContext().getString(R.string.color_key_detail), 0)

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

            val isNotificationRepeating = repeatingCheckBox.isChecked
            val notificationRepeatInterval = if (isNotificationRepeating) preferences.getLong(
                getString(R.string.repeat_interval_key),
                0
            ) else 0

            notificationTitle = title.text.toString()
            notificationText = text.text.toString()

            handleAlarmsDetail(
                requireContext(),
                detailViewModel,
                receivedArgs.requestCode,
                calendar,
                timePicker.minute,
                timePicker.hour,
                calendar.timeInMillis,
                notificationTitle,
                notificationText,
                isNotificationRepeating,
                notificationRepeatInterval,
                color,
                currentChannel
            )

            // calculating the exact difference in days, hours, minutes from now until the set reminder
            // did this "top down", first calculating the days, then the remaining hours and minutes. you could do this the other way around it just came to me like this
            val diffInDaysExact =
                (calendar.timeInMillis - Calendar.getInstance().timeInMillis).toDouble() / 1000 / 60 / 60 / 24
            val diffInDays = diffInDaysExact.toInt()
            val hoursRestExact = diffInDaysExact % 1 * 24
            val hoursRest = hoursRestExact.toInt()
            val minutesRest = (hoursRestExact % 1 * 60).toInt()

            // this is just deciding on a fitting string to tell the user how long it is until the reminder will go off, don't think about it too much
            when {
                diffInDays >= 31 -> Toast.makeText(
                    requireContext(),
                    getString(R.string.more_than_a_month),
                    Toast.LENGTH_LONG
                ).show()

                diffInDays >= 365 -> Toast.makeText(
                    requireContext(),
                    getString(R.string.more_than_a_year),
                    Toast.LENGTH_LONG
                ).show()

                diffInDays == 0 && hoursRest == 0 && minutesRest == 0 -> Toast.makeText(
                    requireContext(),
                    getString(R.string.less_than_a_minute),
                    Toast.LENGTH_LONG
                ).show()

                diffInDays == 0 && hoursRest == 0 -> Toast.makeText(
                    requireContext(),
                    getString(R.string.remind_you_in_start) + minutesRest + " " + (if (minutesRest > 1) getString(
                        R.string.remind_you_in_minutes
                    ) else getString(R.string.remind_you_in_minute)) + ".", Toast.LENGTH_LONG
                ).show()

                diffInDays == 0 && minutesRest == 0 -> Toast.makeText(
                    requireContext(),
                    getString(R.string.remind_you_in_start) + hoursRest + " " + (if (hoursRest > 1) getString(
                        R.string.remind_you_in_hours
                    ) else getString(R.string.remind_you_in_hour)) + ".", Toast.LENGTH_LONG
                ).show()

                diffInDays == 0 -> Toast.makeText(
                    requireContext(),
                    getString(R.string.remind_you_in_start) + hoursRest + " " + (if (hoursRest > 1) getString(
                        R.string.remind_you_in_hours
                    ) else getString(R.string.remind_you_in_hour)) + getString(R.string.remind_you_in_and) + minutesRest + " " + (if (minutesRest > 1) getString(
                        R.string.remind_you_in_minutes
                    ) else getString(R.string.remind_you_in_minute)) + ".", Toast.LENGTH_LONG
                ).show()

                hoursRest == 0 && minutesRest == 0 -> Toast.makeText(
                    requireContext(),
                    getString(R.string.remind_you_in_start) + diffInDays + " " + if (diffInDays > 1) getString(
                        R.string.remind_you_in_days
                    ) else getString(R.string.remind_you_in_day), Toast.LENGTH_LONG
                ).show()

                hoursRest == 0 -> Toast.makeText(
                    requireContext(),
                    getString(R.string.remind_you_in_start) + diffInDays + " " + (if (diffInDays > 1) getString(
                        R.string.remind_you_in_days
                    ) else getString(R.string.remind_you_in_day)) + getString(R.string.remind_you_in_and) + minutesRest + " " + (if (minutesRest > 1) getString(
                        R.string.remind_you_in_minutes
                    ) else getString(R.string.remind_you_in_minute)) + ".", Toast.LENGTH_LONG
                ).show()

                minutesRest == 0 -> Toast.makeText(
                    requireContext(),
                    getString(R.string.remind_you_in_start) + diffInDays + " " + (if (diffInDays > 1) getString(
                        R.string.remind_you_in_days
                    ) else getString(R.string.remind_you_in_day)) + getString(R.string.remind_you_in_and) + hoursRest + " " + (if (hoursRest > 1) getString(
                        R.string.remind_you_in_hours
                    ) else getString(R.string.remind_you_in_hour)) + ".", Toast.LENGTH_LONG
                ).show()

                else -> Toast.makeText(
                    requireContext(),
                    getString(R.string.remind_you_in_start) + diffInDays + " " + (if (diffInDays > 1) getString(
                        R.string.remind_you_in_days
                    ) else getString(R.string.remind_you_in_day)) + ", " + hoursRest + " " + (if (hoursRest > 1) getString(
                        R.string.remind_you_in_hours
                    ) else getString(R.string.remind_you_in_hour)) + getString(R.string.remind_you_in_and) + minutesRest + " " + (if (minutesRest > 1) getString(
                        R.string.remind_you_in_minutes
                    ) else getString(R.string.remind_you_in_minute)) + ".", Toast.LENGTH_LONG
                ).show()
            }

            hideSoftKeyboard(requireContext(), requireView())

            requireActivity().onBackPressed()
        }

        return binding.root
    }

//    fun detailKeyUp(keyCode: Int, event: KeyEvent?) {
//        if (keyCode == KeyEvent.KEYCODE_NAVIGATE_NEXT) {
//            println("next key pressed detail")
//        }
//    }
}