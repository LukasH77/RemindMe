package com.example.fancytimes.home

import android.app.*
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.*
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationManagerCompat
import androidx.fragment.app.Fragment
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.example.fancytimes.R
import com.example.fancytimes.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(layoutInflater, R.layout.fragment_home, container, false)
        setHasOptionsMenu(true)





        val instantNotificationButton = binding.bInstantNotification

        createNotificationChannel()

        val notification =
            Notification.Builder(requireActivity(), "FancyTimes notifications")
                .setSmallIcon(R.drawable.access_time_24px)
                .setContentTitle("Fancy Time!!").setContentText("It's time, it is fancy o'clock!")

        instantNotificationButton.setOnClickListener {
            with(NotificationManagerCompat.from(requireActivity())) {
                notify(1, notification.build())
            }
        }

        return binding.root
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Notification Channel"
            val text = "Fancy Time notifications"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("FancyTimes notifications", name, importance).apply {
                description = text
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
        return NavigationUI.onNavDestinationSelected(
            item,
            requireView().findNavController()
        ) || super.onOptionsItemSelected(item)
    }
}