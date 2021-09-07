package com.example.fancytimes

import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.widget.ImageButton
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.NestedScrollView
import androidx.navigation.findNavController
import com.example.fancytimes.detail.DetailFragment
import com.example.fancytimes.home.HomeFragment
import com.example.fancytimes.home.HomeFragmentDirections
import com.example.fancytimes.setter.SetterFragment
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener

class MainActivity : AppCompatActivity(), ColorPickerDialogListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
//        println(this.intent)
        if (this.intent.getIntExtra(getString(R.string.notification_click_identifier_extra_name), 0) == 1) {
//            println("clickIdentifier = 1")
            val clickedNotificationRequestCode = this.intent.getIntExtra(getString(R.string.notification_requestCode_extra_name), 0)
            findNavController(R.id.nav_host).navigate(HomeFragmentDirections.actionHomeFragmentToDetailFragment(clickedNotificationRequestCode))
        }
//        NavigationUI.setupActionBarWithNavController(this, findNavController(R.id.nav_host))
    }

    override fun onSupportNavigateUp(): Boolean {
        return findNavController(R.id.nav_host).navigateUp()
    }

    override fun onColorSelected(dialogId: Int, color: Int) {
        val preferences =
            getSharedPreferences(getString(R.string.notification_preferences_key), MODE_PRIVATE)
        val setterFragment = this.findViewById<NestedScrollView>(R.id.svSetterRoot)
        val isOnScreen = setterFragment != null
//        println("isOnScreen: $isOnScreen")
        if (isOnScreen) {
//            println("color put: $color")
            with(preferences.edit()) {
                this.putInt(getString(R.string.color_key_setter), color)
                this.apply()
            }  // put in selected color
        } else {
            with(preferences.edit()) {
                this.putInt(getString(R.string.color_key_detail), color)
                this.apply()
            }  // put in selected color
        }
//        println("${preferences.getInt(getString(R.string.color_key_setter), 0)} \n ${preferences.getInt(getString(R.string.color_key_detail), 0)}")
        this.findViewById<TextView>(R.id.tvColorPreview).setBackgroundColor(color)
    }

    override fun onDialogDismissed(dialogId: Int) {}

//    override fun onBackPressed() {
//        val deletionActivator = this.findViewById<ImageButton>(R.id.ibDeleteReminders)
//        val isOnScreen = deletionActivator != null
//        val isSelectActive =
//            deletionActivator?.getTag(R.string.isSelectActive_from_activity)?.equals("active")
//        if (isOnScreen && isSelectActive == true) {
//            HomeFragment.isSelectActive.value = false
//        } else {
//            super.onBackPressed()
//        }
//    }
}