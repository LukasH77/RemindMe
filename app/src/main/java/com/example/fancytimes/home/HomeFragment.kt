package com.example.fancytimes.home

import android.app.*
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.format.DateFormat
import android.view.*
import android.widget.ImageButton
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import com.example.fancytimes.FancyTimeBroadcast
import com.example.fancytimes.R
import com.example.fancytimes.database.ReminderDatabase
import com.example.fancytimes.databinding.FragmentHomeBinding
import com.example.fancytimes.hideSoftKeyboard
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.coroutines.selects.select

class HomeFragment : Fragment() {


    // TODO 1. DONE
    //  -> counter preference goes up with each set alarm
    //  -> each set alarm gets its own preference with the current counter value as key & value

    // TODO 3. DONE
    //  -> allow manual cancelling of alarms via their preference key, deleting the corresponding preference

    // TODO 4. DONE
    //  -> visualize all active alarms using a database and recycler view

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

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val preferences = activity?.getSharedPreferences(
            getString(R.string.notification_preferences_key),
            Context.MODE_PRIVATE
        )

        createNotificationChannel()

        if (preferences!!.getInt(getString(R.string.notification_channel_count), 1000) == 1000) {
            println("channel count not initialized")
            with(preferences.edit()) {
                this.putInt(getString(R.string.notification_channel_count), 1)
                this.apply()
            }
        }


        val reminderDao =
            ReminderDatabase.createInstance(requireContext()).reminderDao

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
                binding.tvHeader.text = "Reminders"
                binding.cbAll.isChecked = false
                binding.ibDeleteReminders.setImageResource(R.drawable.delete_24px)
            }
        })

        selectCount.observe(viewLifecycleOwner, {
            if (isSelectActive.value == true) {
                binding.tvHeader.text = "${selectCount.value} Selected"
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

        binding.cbAll.setOnCheckedChangeListener { cbAll, isChecked ->
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
                Toast.makeText(requireContext(), "There are no Reminders to cancel", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (isSelectActive.value == false) {
                isSelectActive.value = true
            } else if (isSelectActive.value == true) {
                isSelectActive.value = false
            }
        }

        addReminder.setOnClickListener {
            isSelectActive.value = false
            it.findNavController()
                .navigate(HomeFragmentDirections.actionHomeFragmentToSetterFragment())
        }


//        binding.ibDeleteReminders.setOnClickListener {
//            isSelectActive.value = !isSelectActive.value!!
//            if (isSelectActive.value!!) {
//                binding.tvHeader.text = "0 Selected"
//                binding.cbAll.visibility = View.VISIBLE
//                binding.ibDeleteReminders.setImageResource(R.drawable.cancel_24px)
//            } else {
//                binding.cbAll.isChecked = false
//                binding.cbAll.visibility = View.GONE
//                binding.ibDeleteReminders.setImageResource(R.drawable.delete_24px)
//                binding.tvHeader.text = "Reminders"
//            }
//        }
//
//        var isChecked = false
//        binding.cbAll.setOnCheckedChangeListener { compoundButton, b ->
//            isChecked = !isChecked
//            if (isChecked) {
//                binding.tvHeader.text = "*all* Selected"
//                isSelectAll.value = true
//            } else {
//                binding.tvHeader.text = "*all* Selected"
//                isSelectAll.value = false
//            }
//        }

        binding.bRemoveAll.setOnClickListener {
            if (selectCount.value == 0) {
                Toast.makeText(requireContext(), "No reminders selected", Toast.LENGTH_SHORT).show()
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
            AlertDialog.Builder(requireContext()).setTitle("Clear all")
                .setMessage(if (selectCount.value == 1) "Do you really want to cancel the selected reminder?" else "Do you really want to cancel selected reminders?").setPositiveButton(
                    "Yes"
                ) { _: DialogInterface, _: Int ->
                    isRemovalReady.value = true
//                    val intent = Intent(requireContext(), FancyTimeBroadcast::class.java)
////                    println("Request code key $requestCodeMax")
//                    for (i in reminderAdapter.currentList) {
//                        println("selected")
//                        if (i.selected == true) {
//                            try {
//                                alarmManager.cancel(
//                                    PendingIntent.getBroadcast(
//                                        requireContext(),
//                                        i.requestCode,
//                                        intent,
//                                        PendingIntent.FLAG_NO_CREATE
//                                    )
//                                )
//                                homeViewModel.deleteByRequestCode(i.requestCode)
//                            } catch (e: Exception) {
//                                println("cancel() called with a null PendingIntent")
//                            }
//                            with(preferences!!.edit()) {
//                                this.remove(i.requestCode.toString())
//                                this.apply()
//                            }
//                        }
//                    }
                    isSelectActive.value = false
                    println("fragment transaction")
                    parentFragmentManager.beginTransaction().detach(this).attach(this).commit()
//                    val homeFragment = parentFragmentManager.findFragmentByTag()
//                    val fragmentTransaction = parentFragmentManager.beginTransaction()
//                    println("fragment transaction")
//                    fragmentTransaction.detach(homeFragment)
//                    fragmentTransaction.attach(homeFragment)
//                    fragmentTransaction.commit()
                    isRemovalReady.value = false
                    it.findNavController()
                        .navigate(HomeFragmentDirections.actionHomeFragmentToTrashFragment())
                }.setNegativeButton("No", null).setIcon(android.R.drawable.ic_dialog_alert).show()
            isRemovalReady.value = false
        }
        return binding.root
    }

    private fun createNotificationChannel() {
//        val notificationChannels = this.resources.getStringArray(R.array.notificationChannels)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val channel = NotificationChannel(
                getString(R.string.notification_channel),
                "Notification Channel",
                NotificationManager.IMPORTANCE_HIGH
            )
            channel.enableVibration(true)
            channel.apply { description = "Reminders" }
//            val channel2 = NotificationChannel(
//                notificationChannels[1],
//                "Notification Channel",
//                NotificationManager.IMPORTANCE_HIGH
//            ).apply {
//                description = "Reminders"
//            }
//            val channel3 = NotificationChannel(
//                notificationChannels[2],
//                "Notification Channel",
//                NotificationManager.IMPORTANCE_HIGH
//            ).apply {
//                description = "Reminders"
//            }
//            val channel4 = NotificationChannel(
//                notificationChannels[3],
//                "Notification Channel",
//                NotificationManager.IMPORTANCE_HIGH
//            ).apply {
//                description = "Reminders"
//            }
//            val channel5 = NotificationChannel(
//                notificationChannels[4],
//                "Notification Channel",
//                NotificationManager.IMPORTANCE_HIGH
//            ).apply {
//                description = "Reminders"
//            }
//            val channel6 = NotificationChannel(
//                notificationChannels[5],
//                "Notification Channel",
//                NotificationManager.IMPORTANCE_HIGH
//            ).apply {
//                description = "Reminders"
//            }
//            val channel7 = NotificationChannel(
//                notificationChannels[6],
//                "Notification Channel",
//                NotificationManager.IMPORTANCE_HIGH
//            ).apply {
//                description = "Reminders"
//            }
//            val channel8 = NotificationChannel(
//                notificationChannels[7],
//                "Notification Channel",
//                NotificationManager.IMPORTANCE_HIGH
//            ).apply {
//                description = "Reminders"
//            }
//            val channel9 = NotificationChannel(
//                notificationChannels[8],
//                "Notification Channel",
//                NotificationManager.IMPORTANCE_HIGH
//            ).apply {
//                description = "Reminders"
//            }
//            val channel10 = NotificationChannel(
//                notificationChannels[9],
//                "Notification Channel",
//                NotificationManager.IMPORTANCE_HIGH
//            ).apply {
//                description = "Reminders"
//            }

//            val channels = arrayOf(channel1, channel2, channel3, channel4, channel5, channel6, channel7, channel8, channel9, channel10)

//            for (channel in channels) {
            val notificationManager: NotificationManager =
                activity?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
//            }
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