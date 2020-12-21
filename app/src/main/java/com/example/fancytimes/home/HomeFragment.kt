package com.example.fancytimes.home

import android.app.*
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.*
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import com.example.fancytimes.R
import com.example.fancytimes.database.ReminderDatabase
import com.example.fancytimes.databinding.FragmentHomeBinding
import com.example.fancytimes.hideSoftKeyboard

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

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        createNotificationChannel()
        setHasOptionsMenu(true)

        val reminderDao =
            ReminderDatabase.createInstance(requireContext()).reminderDao

        homeViewModelFactory = HomeViewModelFactory(reminderDao)

        homeViewModel = ViewModelProvider(this, homeViewModelFactory).get(HomeViewModel::class.java)

        binding = DataBindingUtil.inflate(layoutInflater, R.layout.fragment_home, container, false)

        alarmManager = activity?.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val addReminder = binding.bSetReminder

        val preferences = activity?.getSharedPreferences(
            getString(R.string.notification_preferences_key),
            Context.MODE_PRIVATE
        )

        val reminderAdapter = ReminderAdapter(preferences)
        binding.rvReminders.adapter = reminderAdapter
        homeViewModel.reminders.observe(viewLifecycleOwner, {
            println(it)
            it?.let {
                reminderAdapter.submitList(it)
            }
        })

        addReminder.setOnClickListener {
            hideSoftKeyboard(requireContext(), requireView())

            it.findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToSetterFragment())
        }

        binding.bRemoveAll.setOnClickListener {
            homeViewModel.deleteAll()

            with(preferences!!.edit()) {
                this.clear()
                this.putInt(getString(R.string.request_code_key), 0)
                this.apply()
            }
        }

        binding.rvReminders.setOnClickListener {
            hideSoftKeyboard(requireContext(), requireView())
        }

        return binding.root
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                getString(R.string.notification_channel_id),
                "Notification Channel",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminders"
            }
            val notificationManager: NotificationManager =
                activity?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.overflow_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        hideSoftKeyboard(requireContext(), requireView())
        return NavigationUI.onNavDestinationSelected(
            item,
            requireView().findNavController()
        ) || super.onOptionsItemSelected(item)
    }
}