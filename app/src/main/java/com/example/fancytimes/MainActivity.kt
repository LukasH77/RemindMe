package com.example.fancytimes

import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.widget.ImageButton
import android.widget.ScrollView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.NestedScrollView
import androidx.navigation.findNavController
import com.example.fancytimes.detail.DetailFragment
import com.example.fancytimes.home.HomeFragment
import com.example.fancytimes.setter.SetterFragment
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
        val setterFragment = this.findViewById<NestedScrollView>(R.id.svSetterRoot)
        val isOnScreen = setterFragment != null
        println("isOnScreen: $isOnScreen")
        if (isOnScreen) {
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
        println("${preferences.getInt(getString(R.string.color_key_setter), 0)} \n ${preferences.getInt(getString(R.string.color_key_detail), 0)}")
        this.findViewById<TextView>(R.id.tvColorPreview).setBackgroundColor(color)
    }

    override fun onDialogDismissed(dialogId: Int) {}

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onBackPressed() {
        val deletionActivator = this.findViewById<ImageButton>(R.id.ibDeleteReminders)
        val isOnScreen = deletionActivator != null
        val isSelectActive =
            deletionActivator?.getTag(R.string.isSelectActive_from_activity)?.equals("active")
        if (isOnScreen && isSelectActive == true) {
            HomeFragment.isSelectActive.value = false
        } else {
            super.onBackPressed()
        }
    }

    /*@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        println("key up")
        val setterLayout = this.findViewById<NestedScrollView>(R.id.svSetterRoot)
        val detailLayout = this.findViewById<NestedScrollView>(R.id.svDetailRoot)
        if (setterLayout != null) {
            println("setter on screen")
            SetterFragment().setterKeyUp(keyCode, event)
        } else if (detailLayout != null) {
            DetailFragment().detailKeyUp(keyCode, event)
            println("detail on screen")
        }
        return super.onKeyUp(keyCode, event)
    }*/
}