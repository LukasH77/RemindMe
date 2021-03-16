package com.example.fancytimes

import android.content.SharedPreferences
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.example.fancytimes.home.HomeFragment
import com.jaredrummler.android.colorpicker.ColorPickerDialog
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener

class MainActivity : AppCompatActivity(), ColorPickerDialogListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
//        NavigationUI.setupActionBarWithNavController(this, findNavController(R.id.nav_host))
    }

    override fun onSupportNavigateUp(): Boolean {
        return findNavController(R.id.nav_host).navigateUp()
    }

    override fun onColorSelected(dialogId: Int, color: Int) {
        val preferences =
            getSharedPreferences(getString(R.string.notification_preferences_key), MODE_PRIVATE)
        with(preferences.edit()) {
            this.putInt(getString(R.string.color_key), color)
            this.apply()
        }
        this.findViewById<TextView>(R.id.tvColorPreview).setBackgroundColor(color)
    }

    override fun onDialogDismissed(dialogId: Int) {}

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onBackPressed() {
        val deletionActivator = this.findViewById<ImageButton>(R.id.ibDeleteReminders)
        val isOnScreen =  deletionActivator != null
        val isSelectActive = deletionActivator?.getTag(R.string.isSelectActive_from_activity)?.equals("active")
        if (isOnScreen && isSelectActive == true) {
            HomeFragment.isSelectActive.value = false
        } else {
            super.onBackPressed()
        }
    }
}