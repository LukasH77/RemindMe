package com.example.fancytimes.detail

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.format.DateFormat
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.CompoundButton
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import com.example.fancytimes.*
import com.example.fancytimes.database.ReminderDatabase
import com.example.fancytimes.databinding.FragmentDetailBinding
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

        val preferences = activity?.getSharedPreferences(getString(R.string.notification_preferences_key), Context.MODE_PRIVATE)

        val datePicker = DateSetter()
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
            if (checkedState) repeatingIntervalsSpinner.visibility = View.VISIBLE else repeatingIntervalsSpinner.visibility = View.INVISIBLE
        }

        val system24hrs = DateFormat.is24HourFormat(requireContext())
        timePicker.setIs24HourView(system24hrs)

        binding.bEditDate.setOnClickListener {
            datePicker.show(parentFragmentManager, "Date Picker")
        }

        binding.tvRequestCode.text = "Request Code: ${receivedArgs.requestCode}"

        detailViewModel.selectedReminder.observe(viewLifecycleOwner, Observer {
            it?.let {
                binding.tpTimePicker.minute = it.minute
                binding.tpTimePicker.hour = it.hour
                binding.etNotificationTitle.text = SpannableStringBuilder(it.title)
                binding.etNotificationText.text = SpannableStringBuilder(it.text)
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
        repeatingIntervalsSpinner.setSelection(5)

        binding.bConfirmPick.setOnClickListener {
            val calendar = Calendar.getInstance()

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
            }

            val isNotificationRepeating = repeatingCheckBox.isChecked
            val notificationRepeatInterval = if (isNotificationRepeating) preferences.getLong(getString(R.string.repeat_interval_key), 0) else 0

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
                notificationRepeatInterval
            )

            hideSoftKeyboard(requireContext(), requireView())

            it.findNavController().navigate(DetailFragmentDirections.actionDetailFragmentToHomeFragment())
        }

        return binding.root
    }
}