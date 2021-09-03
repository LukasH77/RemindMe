package com.example.fancytimes.home

import android.app.*
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.example.fancytimes.FancyTimeBroadcast
import com.example.fancytimes.R
import com.example.fancytimes.database.ReminderDatabase
import com.example.fancytimes.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding

    private lateinit var alarmManager: AlarmManager

    private lateinit var homeViewModel: HomeViewModel
    private lateinit var homeViewModelFactory: HomeViewModelFactory

    companion object {
        val isSelectActive: MutableLiveData<Boolean> = MutableLiveData()
        val isSelectAll: MutableLiveData<Boolean> = MutableLiveData()
        val isDirectSelectAll: MutableLiveData<Boolean> = MutableLiveData()
        val selectCount: MutableLiveData<Int> = MutableLiveData()
        val isRemovalReady: MutableLiveData<Boolean> = MutableLiveData()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val preferences = activity?.getSharedPreferences(
            getString(R.string.notification_preferences_key),
            Context.MODE_PRIVATE
        )

        createNotificationChannel()

        if (preferences!!.getInt(getString(R.string.notification_channel_count), -1) == -1) {
            println("channel count not initialized")
            with(preferences.edit()) {
                this.putInt(getString(R.string.notification_channel_count), 1)
                this.putBoolean(getString(R.string.repeat_preference_key), true)
                this.apply()
            }
        }

        val dbInstance = ReminderDatabase.createInstance(requireContext())
        val reminderDao = dbInstance.reminderDao

        homeViewModelFactory = HomeViewModelFactory(reminderDao)

        homeViewModel = ViewModelProvider(this, homeViewModelFactory).get(HomeViewModel::class.java)

        binding = DataBindingUtil.inflate(layoutInflater, R.layout.fragment_home, container, false)

        alarmManager = activity?.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val addReminder = binding.ibSetReminder

        isSelectActive.value = false
        isSelectAll.value = false
        selectCount.value = 0



        val reminderAdapter =
            ReminderAdapter(
                preferences,
                DateFormat.is24HourFormat(requireContext()),
                homeViewModel,
                viewLifecycleOwner,
                isSelectActive,
                isDirectSelectAll,
                isSelectAll,
                selectCount,
                isRemovalReady,
                alarmManager
            )
        binding.rvReminders.adapter = reminderAdapter
        homeViewModel.reminders.observe(viewLifecycleOwner, {
//            println(it)
            it?.let {
                reminderAdapter.submitList(it)
            }
        })

        isSelectActive.observe(viewLifecycleOwner, {
            if (it) {
                binding.ibDeleteReminders.setTag(R.string.isSelectActive_from_activity, "active")
                binding.bRemoveAll.visibility = View.VISIBLE
                binding.cbAll.visibility = View.VISIBLE
                binding.ibDeleteReminders.setImageResource(R.drawable.cancel_24px)
            } else if (!it) {
                binding.ibDeleteReminders.setTag(R.string.isSelectActive_from_activity, "inactive")
                binding.bRemoveAll.visibility = View.GONE
                binding.cbAll.visibility = View.GONE
                binding.tvHeader.text = getString(R.string.reminders)
                binding.cbAll.isChecked = false
                binding.ibDeleteReminders.setImageResource(R.drawable.delete_24px)
            }
        })

        selectCount.observe(viewLifecycleOwner, {
            if (isSelectActive.value == true) {
                binding.tvHeader.text = getString(R.string.x_selected, selectCount.value)
            }
        })

        isSelectAll.observe(viewLifecycleOwner, {
            if (it) {
                if (!binding.cbAll.isChecked) {
                    binding.cbAll.isChecked = true
                }
            } else if (!it) {
                if (binding.cbAll.isChecked) {
                    binding.cbAll.isChecked = false
                }
            }
        })

        binding.cbAll.setOnCheckedChangeListener { _, isChecked ->
            println("isChecked = $isChecked")
            isSelectAll.value = isChecked
            if (isChecked) {
                selectCount.value = reminderAdapter.itemCount
            } else if (selectCount.value!! == reminderAdapter.itemCount) {
                println(selectCount.value)
                println(reminderAdapter.itemCount)
                isDirectSelectAll.value = false
            }
        }

        binding.ibDeleteReminders.setOnClickListener {
            if (reminderAdapter.itemCount == 0) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.no_reminders_to_cancel),
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }

            AlertDialog.Builder(requireContext()).setTitle(getString(R.string.clear_all))
                .setMessage(requireContext().getString(R.string.delete_all_warning)).setPositiveButton(
                    requireContext().getString(R.string.yes)
                ) { _: DialogInterface, _: Int ->
                    homeViewModel.reminders.observe(viewLifecycleOwner) {
                        val intent = Intent(requireContext(), FancyTimeBroadcast::class.java)
                        for (item in it) {
                            try {
                                alarmManager.cancel(
                                    PendingIntent.getBroadcast(
                                        requireContext(),
                                        item.requestCode,
                                        intent,
                                        PendingIntent.FLAG_NO_CREATE
                                    )
                                )
                            } catch (e: Exception) {
                                println("cancel() called with a null PendingIntent")
                            }
                            with(preferences.edit()) {
                                this.remove(item.requestCode.toString())
                                this.apply()
                            }
                        }
                    }
                    homeViewModel.deleteAll()
                }.setNegativeButton(requireContext().getString(R.string.no), null).setIcon(android.R.drawable.ic_dialog_alert).show()
//            if (isSelectActive.value == false) {
//                isSelectActive.value = true
//            } else if (isSelectActive.value == true) {
//                isSelectActive.value = false
//            }
        }

        addReminder.setOnClickListener {
            isSelectActive.value = false
            it.findNavController()
                .navigate(HomeFragmentDirections.actionHomeFragmentToSetterFragment())
        }


//        binding.ibDeleteReminders.setOnClickListener {
////            isSelectActive.value = !isSelectActive.value!!
////            if (isSelectActive.value!!) {
////                binding.tvHeader.text = "0 Selected"
////                binding.cbAll.visibility = View.VISIBLE
////                binding.ibDeleteReminders.setImageResource(R.drawable.cancel_24px)
////            } else {
////                binding.cbAll.isChecked = false
////                binding.cbAll.visibility = View.GONE
////                binding.ibDeleteReminders.setImageResource(R.drawable.delete_24px)
////                binding.tvHeader.text = "Reminders"
////            }
////        }
////
////        var isChecked = false
////        binding.cbAll.setOnCheckedChangeListener { compoundButton, b ->
////            isChecked = !isChecked
////            if (isChecked) {
////                binding.tvHeader.text = "*all* Selected"
////                isSelectAll.value = true
////            } else {
////                binding.tvHeader.text = "*all* Selected"
////                isSelectAll.value = false
////            }
////        }

        binding.bRemoveAll.setOnClickListener {
            if (selectCount.value == 0) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.no_reminders_selected),
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }
//            AlertDialog.Builder(requireContext()).setTitle("Clear all")
//                .setMessage("Do you really want to cancel all set reminders?").setPositiveButton(
//                    "Yes"
//                ) { _: DialogInterface, _: Int ->
//                    val requestCodeMax =
//                        preferences!!.getInt(getString(R.string.request_code_key), 0)
//                    val intent = Intent(requireContext(), FancyTimeBroadcast::class.java)
////                    println("Request code key $requestCodeMax")
//                    for (i in 0 until requestCodeMax) {
//                        try {
//                            alarmManager.cancel(
//                                PendingIntent.getBroadcast(
//                                    requireContext(),
//                                    i,
//                                    intent,
//                                    PendingIntent.FLAG_NO_CREATE
//                                )
//                            )
//                        } catch (e: Exception) {
//                            println("cancel() called with a null PendingIntent")
//                        }
//                    }
//                    homeViewModel.deleteAll()
//                    with(preferences.edit()) {
//                        this.clear()
//                        this.putInt(getString(R.string.request_code_key), 0)
//                        this.apply()
//                    }
//                }.setNegativeButton("No", null).setIcon(android.R.drawable.ic_dialog_alert).show()
            AlertDialog.Builder(requireContext()).setTitle(getString(R.string.cancel_selected_reminders_title))
                .setMessage(
                    if (selectCount.value == 1) getString(R.string.confirm_cancelation_singular) else getString(
                        R.string.confirm_cancelation_plural
                    )
                )
                .setPositiveButton(
                    getString(R.string.yes)
                ) { _: DialogInterface, _: Int ->
                    isRemovalReady.value = true
                    isSelectActive.value = false
                    println("fragment transaction")

                    parentFragmentManager.beginTransaction().detach(this).attach(this).commit()
                    isRemovalReady.value = false

                    it.findNavController()
                        .navigate(HomeFragmentDirections.actionHomeFragmentToTrashFragment())
                }.setNegativeButton(getString(R.string.no), null).setIcon(android.R.drawable.ic_dialog_alert).show()
            isRemovalReady.value = false
        }

        return binding.root
    }

    private fun createNotificationChannel() {

        val channel = NotificationChannel(
            getString(R.string.notification_channel),
            "Notification Channel",
            NotificationManager.IMPORTANCE_HIGH
        )
        channel.enableVibration(true)
        channel.apply {
            description = "Reminders"

            val notificationManager: NotificationManager =
                activity?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

}