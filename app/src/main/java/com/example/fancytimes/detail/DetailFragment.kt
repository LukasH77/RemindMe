package com.example.fancytimes.detail

import android.app.DatePickerDialog
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.format.DateFormat
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
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

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val receivedArgs by navArgs<DetailFragmentArgs>()

        val reminderDao = ReminderDatabase.createInstance(requireContext()).reminderDao
        val detailViewModelFactory = DetailViewModelFactory(reminderDao, receivedArgs.requestCode)

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_detail, container, false)
        detailViewModel =
            ViewModelProvider(this, detailViewModelFactory).get(DetailViewModel::class.java)

        val preferences = activity?.getSharedPreferences(
            getString(R.string.notification_preferences_key),
            Context.MODE_PRIVATE
        )

        val calendar = Calendar.getInstance()
//        val datePicker = DateSetter(preferences!!)

        var setDay = 0
        var setMonth = 0
        var setYear = 0

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

        val datePicker = DatePickerDialog(
            requireContext(),
            { _: DatePicker?, year: Int, month: Int, day: Int ->
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

                binding.tvSetDate.text =
                    "${if (setDay < 10) "0$setDay" else setDay}.${if (setMonth < 9) "0${setMonth + 1}" else setMonth + 1}.$setYear"
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePicker.datePicker.minDate = System.currentTimeMillis()

        val timePicker = binding.tpTimePicker
        val title = binding.etNotificationTitle
        val text = binding.etNotificationText
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

        binding.ibEditDate.setOnClickListener {
            datePicker.show()
            hideSoftKeyboard(requireContext(), requireView())
        }

        var currentChannel = 5
        detailViewModel.selectedReminder.observe(viewLifecycleOwner, {
            it?.let {
                currentChannel = it.notificationChannel
                println("Detail current channel: $currentChannel")

                val color = it.color
                colorPicker.setColor(color)
                binding.tvColorPreview.setBackgroundColor(color)

                setDay = it.day
                setMonth = it.month
                setYear = it.year
                with(preferences.edit()) {
                    this.putInt(getString(R.string.color_key_detail), color)
                    this.putInt(requireContext().getString(R.string.day_key), setDay)
                    this.putInt(requireContext().getString(R.string.month_key), setMonth)
                    this.putInt(requireContext().getString(R.string.year_key), setYear)
                    this.apply()
                }

                binding.tvSetDate.text =
                    "${if (it.day < 10) "0${it.day}" else it.day}.${if (it.month < 9) "0${it.month + 1}" else it.month + 1}.${it.year}  (${it.requestCode})"
                binding.tpTimePicker.minute = it.minute
                binding.tpTimePicker.hour = it.hour
                title.text = SpannableStringBuilder(it.title)
                text.text = SpannableStringBuilder(it.text)
                datePicker.updateDate(it.year, it.month, it.day)
                if (it.repetition != 0L) {
                    binding.cbRepeating.isChecked = true
                    when (it.repetition) {
                        60000L -> repeatingIntervalsSpinner.setSelection(0)
                        600000L -> repeatingIntervalsSpinner.setSelection(1)
                        1800000L -> repeatingIntervalsSpinner.setSelection(2)
                        3600000L -> repeatingIntervalsSpinner.setSelection(3)
                        86400000L -> repeatingIntervalsSpinner.setSelection(4)
                        1L -> repeatingIntervalsSpinner.setSelection(5)
                        2L -> repeatingIntervalsSpinner.setSelection(6)
                    }
                }
            }
        })

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

        repeatingIntervalsSpinner.setSelection(4)

        val temp = arrayOf(binding.tvColorPreview, binding.cbRepeating, binding.tvSetDate, binding.tpTimePicker, binding.clMainLayout)

        for (i in temp) {
            i.setOnClickListener {
                hideSoftKeyboard(requireContext(), requireView())
            }
        }

        binding.ibEditColor.setOnClickListener {
            colorPicker.setColor(preferences.getInt(requireContext().getString(R.string.color_key_detail), 0))
            colorPicker.show(requireActivity())
            hideSoftKeyboard(requireContext(), requireView())
        }

        binding.bConfirmPick.setOnClickListener {

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
                (hourIsTooEarly || hourIsEqual) && timePicker.minute < Calendar.getInstance()
                    .get(Calendar.MINUTE)

            if (yearIsTooEarly || monthIsTooEarly || dayIsTooEarly) {
                Toast.makeText(requireContext(), getString(R.string.invalid_date), Toast.LENGTH_SHORT).show()
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

            hideSoftKeyboard(requireContext(), requireView())

            requireActivity().onBackPressed()
        }

        return binding.root
    }
}