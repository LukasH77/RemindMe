package com.example.fancytimes

import android.content.SharedPreferences
import android.view.View
import android.widget.AdapterView

class IntervalSetter(private val preferences: SharedPreferences) :
    AdapterView.OnItemSelectedListener {
    override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
//        println("Spinner pos: $pos")
        with(preferences.edit()) {
            when (pos) {
                0 -> this.putLong(view!!.context.getString(R.string.repeat_interval_key), 300000) // 5 minutes
                1 -> this.putLong(view!!.context.getString(R.string.repeat_interval_key), 900000) // 15 minutes
                2 -> this.putLong(view!!.context.getString(R.string.repeat_interval_key), 1800000) // 30 minutes
                3 -> this.putLong(view!!.context.getString(R.string.repeat_interval_key), 3600000) // 1 hour
                4 -> this.putLong(view!!.context.getString(R.string.repeat_interval_key), 86400000) // 1 day
                5 -> this.putLong(view!!.context.getString(R.string.repeat_interval_key), 604800000) // 1 week
                6 -> this.putLong(view!!.context.getString(R.string.repeat_interval_key), 1) // 1 month
                7 -> this.putLong(view!!.context.getString(R.string.repeat_interval_key), 2) // 1 year
                else -> this.putLong(view!!.context.getString(R.string.repeat_interval_key), 0)  // error
            }
            this.apply()
        }
        hideSoftKeyboard(view!!.context, view)
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {

    }
}