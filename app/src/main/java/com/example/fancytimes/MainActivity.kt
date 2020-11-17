package com.example.fancytimes

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        NavigationUI.setupActionBarWithNavController(this, findNavController(R.id.nav_host))
    }

    override fun onSupportNavigateUp(): Boolean {
        return findNavController(R.id.nav_host).navigateUp()
    }


    //A failed experiment, for now at least.
//    override fun onBackPressed() {
//
//        val timePicker = findViewById<TimePicker>(R.id.tpTimePicker)
//        val exitTp = findViewById<ImageButton>(R.id.ibExitTimePicker)
//        val confirmSelect = findViewById<Button>(R.id.bConfirmPick)
//        val test = findViewById<Button>(R.id.bAddCustomTime)
//        println("Back pressed")
//        if (timePicker.visibility == View.VISIBLE) {
////            timePicker.visibility = View.GONE
////            exitTp.visibility = View.GONE
////            confirmSelect.visibility = View.GONE
////            bSwitchNotifications.visibility = View.VISIBLE
////            bAddCustomTime.visibility = View.VISIBLE
//        } else super.onBackPressed()
//    }
}