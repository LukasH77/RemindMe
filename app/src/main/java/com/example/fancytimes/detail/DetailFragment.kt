package com.example.fancytimes.detail

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.text.SpannableStringBuilder
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import com.example.fancytimes.R
import com.example.fancytimes.database.ReminderDatabase
import com.example.fancytimes.databinding.FragmentDetailBinding
import com.example.fancytimes.handleAlarmsDetail
import com.example.fancytimes.hideSoftKeyboard
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
    ): View? {
        val receivedArgs by navArgs<DetailFragmentArgs>()

        val reminderDao = ReminderDatabase.createInstance(requireContext()).reminderDao
        val detailViewModelFactory = DetailViewModelFactory(reminderDao, receivedArgs.requestCode)

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_detail, container, false)
        detailViewModel =
            ViewModelProvider(this, detailViewModelFactory).get(DetailViewModel::class.java)

        val timePicker = binding.tpTimePicker
        val title = binding.etNotificationTitle
        val text = binding.etNotificationText
        val repetition = binding.cbRepeating

        binding.tpTimePicker.setIs24HourView(true)

        binding.tvRequestCode.text = "Request Code: ${receivedArgs.requestCode}"

        detailViewModel.selectedReminder.observe(viewLifecycleOwner, Observer {
            it?.let {
                binding.tpTimePicker.minute = it.minute
                binding.tpTimePicker.hour = it.hour
                binding.etNotificationTitle.text = SpannableStringBuilder(it.title)
                binding.etNotificationText.text = SpannableStringBuilder(it.text)
                if (it.repetition != null) binding.cbRepeating.isChecked = true
            }
        })

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

            notificationTitle = title.text.toString()
            notificationText = text.text.toString()

            println("IsRepeatingSent ${repetition.isChecked}")

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
                repetition.isChecked
            )

            hideSoftKeyboard(requireContext(), requireView())

            it.findNavController().navigate(DetailFragmentDirections.actionDetailFragmentToHomeFragment())
        }

        return binding.root
    }
}