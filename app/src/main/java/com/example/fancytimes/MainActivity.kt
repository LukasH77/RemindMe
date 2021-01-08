package com.example.fancytimes

import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener

class MainActivity : AppCompatActivity(), ColorPickerDialogListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        NavigationUI.setupActionBarWithNavController(this, findNavController(R.id.nav_host))
    }

    override fun onSupportNavigateUp(): Boolean {
        return findNavController(R.id.nav_host).navigateUp()
    }

    override fun onColorSelected(dialogId: Int, color: Int) {
        val preferences = getSharedPreferences(getString(R.string.notification_preferences_key), MODE_PRIVATE)
        with(preferences.edit()) {
            this.putInt(getString(R.string.color_key), color)
            this.apply()
        }
        this.findViewById<TextView>(R.id.tvColorPreview).setBackgroundColor(color)
    }

    override fun onDialogDismissed(dialogId: Int) {

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