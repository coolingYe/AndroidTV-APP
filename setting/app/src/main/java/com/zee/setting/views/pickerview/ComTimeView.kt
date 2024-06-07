package com.zee.setting.views.pickerview

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import com.zee.setting.R
import java.util.*

class ComTimeView : FrameLayout {

    lateinit var timeSetter: TimeSetter
    var setTimeSelectListener: ((View, String) -> Unit)? = null

    constructor(context: Context) : super(context) {
        initView(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initView(context)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        initView(context)
    }

    private fun initView(context: Context) {
        LayoutInflater.from(context).inflate(R.layout.layout_com_time_view, this)

        val timePickerView = findViewById<View>(R.id.timepicker)
        timeSetter = TimeSetter(timePickerView)

        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()
        val hour = calendar[Calendar.HOUR_OF_DAY]
        val minute = calendar[Calendar.MINUTE]

        timeSetter.setPicker(startHourTime = hour)
    }

    fun setClickView(view: View?) {
        view?.setOnClickListener { v ->
            setTimeSelectListener?.invoke(v, timeSetter.time)
        }
    }

    fun setTime(startDate: Date?, endDate: Date?) {
        val calendarStart = Calendar.getInstance()
        val calendarEnd = Calendar.getInstance()
        startDate?.let {
            calendarStart.time = it
        } ?: run {
            calendarStart.timeInMillis = System.currentTimeMillis()
        }
        endDate?.let {
            calendarEnd.time = it
        } ?: run {
            calendarEnd.timeInMillis = System.currentTimeMillis()
        }

        val hourStart = calendarStart[Calendar.HOUR_OF_DAY]
        val minuteStart = calendarStart[Calendar.MINUTE]
        val hourEnd = calendarEnd[Calendar.HOUR_OF_DAY]
        val minuteEnd = calendarEnd[Calendar.MINUTE]
        timeSetter.setPicker(startHourTime = hourStart, startMinTime = minuteStart, endHourTime = hourEnd, endMinTime = minuteEnd)
    }
}