package com.example.fancytimes

import android.content.SharedPreferences
import android.view.View
import android.widget.AdapterView

class IntervalSetter(private val preferences: SharedPreferences) :
    AdapterView.OnItemSelectedListener {
    override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
//        println("Spinner pos: $pos")
        val oneSecondInMillis = 1000L
        val oneMinuteInMillis = oneSecondInMillis * 60
        val oneHourInMillis = oneMinuteInMillis * 60
        val oneDayInMillis = oneHourInMillis * 24
        val oneWeekInMillis = oneDayInMillis * 7
        with(preferences.edit()) {
            when (pos) {
                0 -> this.putLong(view!!.context.getString(R.string.repeat_interval_key), oneMinuteInMillis * 5) // 5 minutes
                1 -> this.putLong(view!!.context.getString(R.string.repeat_interval_key), oneMinuteInMillis * 15) // 15 minutes
                2 -> this.putLong(view!!.context.getString(R.string.repeat_interval_key), oneMinuteInMillis * 30) // 30 minutes
                3 -> this.putLong(view!!.context.getString(R.string.repeat_interval_key), oneHourInMillis) // 1 hour
                4 -> this.putLong(view!!.context.getString(R.string.repeat_interval_key), oneDayInMillis) // 1 day
                5 -> this.putLong(view!!.context.getString(R.string.repeat_interval_key), oneWeekInMillis) // 1 week
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