package com.zee.setting.views.pickerview

import android.view.View
import com.zee.setting.R

class TimeSetter(var view: View) {

    private lateinit var startHourView: TimeView
    private lateinit var startMinView: TimeView
    private lateinit var endHourView: TimeView
    private lateinit var endMinView: TimeView

     fun setPicker(startHourTime: Int, startMinTime: Int = 0, endHourTime: Int = startHourTime, endMinTime: Int = 10) {
         startHourView = view.findViewById(R.id.start_hour) as TimeView
         startHourView.setCurrentValue(startHourTime)

         startMinView = view.findViewById(R.id.start_minute) as TimeView
         startMinView.setCurrentValue(startMinTime)

         endHourView = view.findViewById(R.id.end_hour) as TimeView
         endHourView.setCurrentValue(endHourTime)

         endMinView = view.findViewById(R.id.end_minute) as TimeView
         endMinView.setCurrentValue(endMinTime)

         startHourView.requestFocus()
     }

    fun setCyclic(cyclic: Boolean) {
        startHourView.setCyclic(cyclic)
        startMinView.setCyclic(cyclic)
        endHourView.setCyclic(cyclic)
        endMinView.setCyclic(cyclic)
    }

    val time: String
    get() {
        val sb = StringBuffer().apply {
            append(startHourView.getCurrentValue()).append(":")
                .append(startMinView.getCurrentValue()).append("~")
                .append(endHourView.getCurrentValue()).append(":")
                .append(endMinView.getCurrentValue())
        }
        return sb.toString()
    }
}