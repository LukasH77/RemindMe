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
                0 -> this.putLong(view!!.context.getString(R.string.repeat_interval_key), 60000)
                1 -> this.putLong(view!!.context.getString(R.string.repeat_interval_key), 600000)
                2 -> this.putLong(view!!.context.getString(R.string.repeat_interval_key), 1800000)
                3 -> this.putLong(view!!.context.getString(R.string.repeat_interval_key), 3600000)
                4 -> this.putLong(view!!.context.getString(R.string.repeat_interval_key), 86400000)
                5 -> this.putLong(view!!.context.getString(R.string.repeat_interval_key), 604800000)
                6 -> this.putLong(view!!.context.getString(R.string.repeat_interval_key), 1)
                7 -> this.putLong(view!!.context.getString(R.string.repeat_interval_key), 2)
                else -> this.putLong(view!!.context.getString(R.string.repeat_interval_key), 0)
            }
            this.apply()
        }
        hideSoftKeyboard(view!!.context, view)
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {

    }
}