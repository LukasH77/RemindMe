package com.example.fancytimes

import android.app.DatePickerDialog
import android.app.Dialog
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.widget.DatePicker
import androidx.annotation.RequiresApi
import androidx.fragment.app.DialogFragment
import java.util.*

class DateSetter(private val preferences: SharedPreferences) : DialogFragment(), DatePickerDialog.OnDateSetListener {

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val calendar = Calendar.getInstance()
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val month = calendar.get(Calendar.MONTH)
        val year = calendar.get(Calendar.YEAR)
        return DatePickerDialog(requireContext(), this, year, month, day)
    }

    override fun onDateSet(datePicker: DatePicker?, year: Int, month: Int, day: Int) {
        println("date set")
        with(preferences.edit()) {
            this.putInt(requireContext().getString(R.string.day_key), day)
            this.putInt(requireContext().getString(R.string.month_key), month)
            this.putInt(requireContext().getString(R.string.year_key), year)
            this.apply()
        }
    }
}