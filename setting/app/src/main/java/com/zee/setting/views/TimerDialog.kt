package com.zee.setting.views

import android.app.AlarmManager
import android.content.Context
import android.content.Context.ALARM_SERVICE
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import com.zee.setting.R
import com.zee.setting.base.BaseActivity
import com.zee.setting.base.BaseConstants.SP_KEY_TIMER_PLAN
import com.zee.setting.cache.AppGlobals.getApplication
import com.zee.setting.cache.SPUtils
import com.zee.setting.databinding.ActivityTimerBinding
import com.zee.setting.receive.alarm.AlarmSetter
import com.zee.setting.receive.alarm.AlarmSetter.AlarmUtil.getTargetCalendar
import com.zee.setting.receive.alarm.AlarmSetter.Companion.dateFormat
import com.zee.setting.utils.DensityUtils
import com.zee.setting.views.pickerview.ComTimeView
import java.util.*

class TimerDialog(
    private val context: Context,
    private val typeFrom: String = TYPE_FROM_ADD,
    private val localDate: String = dateFormat.format(Date())
) : BaseDialog(context), View.OnFocusChangeListener, View.OnClickListener {

    private lateinit var binding: ActivityTimerBinding
    private lateinit var timePickerView: ComTimeView
    private lateinit var alarmManager: AlarmManager
    var setDialogCallback: (() -> Unit?)? =null

    companion object {
        const val TYPE_FROM_ADD = "type_from_add"
        const val TYPE_FROM_EDIT = "type_from_edit"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTimerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        alarmManager = context.getSystemService(ALARM_SERVICE) as AlarmManager

        initView()
        initListener()
    }

    fun initView() {
        typeFrom.let {
            when (it) {
                TYPE_FROM_ADD -> {
                    binding.tvTimerTitle.text = context.getString(R.string.timer_shutdown)
                    binding.tvDateDelete.isVisible = false
                }
                else -> {
                    binding.tvTimerTitle.text = context.getString(R.string.edit)
                    binding.tvDateDelete.isVisible = true
                }
            }
        }

        timePickerView = binding.timeView
        with(timePickerView) {
            setDate()
            setClickView(binding.tvDateApply)
            setTimeSelectListener = object : (View, String) -> Unit {
                override fun invoke(p1: View, p2: String) {
                    addOrEditTask(p2)
                }
            }
            isVisible = true
        }
    }

    private fun setDate() {
        if (typeFrom.contains(TYPE_FROM_EDIT)) {
            localDate.let { time ->
                if (time.isNotBlank()) {
                    val startTime = time.substringBefore("~")
                    val endTime = time.substringAfter("~")
                    val targetStartTime = dateFormat.parse(startTime)
                    val targetEndTime = dateFormat.parse(endTime)
                    timePickerView.setTime(targetStartTime, targetEndTime)
                }
            }
        }
    }

    private fun initListener() {
        binding.tvDateApply.onFocusChangeListener = this
        binding.tvDateCancel.onFocusChangeListener = this
        binding.tvDateDelete.onFocusChangeListener = this
        binding.tvDateCancel.setOnClickListener(this)
        binding.tvDateDelete.setOnClickListener(this)
        binding.imgBack.setOnClickListener(this)
    }

    override fun onFocusChange(v: View?, hasFocus: Boolean) {
        if (v != null) {
            if (v.id == binding.tvDateApply.id) {
                binding.tvDateApply.setTextColor(if (hasFocus) Color.WHITE else Color.BLACK)
            }
            if (v.id == binding.tvDateCancel.id) {
                binding.tvDateCancel.setTextColor(if (hasFocus) Color.WHITE else Color.BLACK)
            }
            if (v.id == binding.tvDateDelete.id) {
                binding.tvDateDelete.setTextColor(if (hasFocus) Color.WHITE else Color.BLACK)
            }
        }
    }

    override fun onClick(v: View?) {
        if (v != null) {
            when (v.id) {
                binding.imgBack.id -> {
                    cancel()
                }
                binding.tvDateCancel.id -> {
                    cancel()
                }
                binding.tvDateDelete.id -> {
                    removeTask(localDate)
                }
            }
        }
    }

    private fun addOrEditTask(targetDate: String?) {
        targetDate?.let { timePlan ->
            val startTime = timePlan.substringBefore("~")
            val endTime = timePlan.substringAfter("~")
            val planSet = HashSet<String>()
            if (checkTask(timePlan)) {
                planSet.addAll(SPUtils.getInstance().getStringSet(SP_KEY_TIMER_PLAN))
                planSet.add(timePlan)
                SPUtils.getInstance().put(SP_KEY_TIMER_PLAN, planSet)
                setAlarmTask(startTime, endTime)
                if (typeFrom.contains(TYPE_FROM_EDIT)) {
                    //remove old task when action for edit
                    planSet.remove(localDate)
                    removeTask(localDate)
                }
                (context as BaseActivity).showToast("设置成功")
                cancel()
            }
        }
    }

    private fun setAlarmTask(startTime: String, endTime: String) {
        // System Sleep Task
        AlarmSetter.AlarmSetterImpl(am = alarmManager, mContext = context)
            .setUpRTCAlarm(
                startTime.hashCode(),
                AlarmSetter.ACTION_SYSTEM_SLEEP,
                getTargetCalendar(startTime)
            )

        // System Wake Up Task
        AlarmSetter.AlarmSetterImpl(am = alarmManager, mContext = context)
            .setUpRTCAlarm(
                endTime.hashCode(),
                AlarmSetter.ACTION_SYSTEM_WAKE_UP,
                getTargetCalendar(endTime)
            )
    }

    private fun removeTask(targetDate: String?) {
        targetDate?.let { timePlan ->
            val startTime = timePlan.substringBefore("~")
            val endTime = timePlan.substringAfter("~")
            val planSet = HashSet<String>()
            planSet.addAll(SPUtils.getInstance().getStringSet(SP_KEY_TIMER_PLAN))
            planSet.remove(timePlan)
            SPUtils.getInstance().put(SP_KEY_TIMER_PLAN, planSet)
            AlarmSetter.AlarmSetterImpl(am = alarmManager, mContext = context)
                .removeRTCAlarm(startTime.hashCode())
            AlarmSetter.AlarmSetterImpl(am = alarmManager, mContext = context)
                .removeRTCAlarm(endTime.hashCode())
            cancel()
        }
    }

    override fun cancel() {
        setDialogCallback?.invoke()
        super.cancel()
    }


    private fun checkTask(targetTime: String): Boolean {
        val planSet = SPUtils.getInstance().getStringSet(SP_KEY_TIMER_PLAN)
        when {
            planSet.contains(targetTime) && localDate.contains(targetTime) -> {
                (context as BaseActivity).showToast("任务未做修改")
                cancel()
                return false
            }
            planSet.contains(targetTime) -> {
                (context as BaseActivity).showToast("任务已存在")
                return false
            }
            AlarmSetter.AlarmUtil.validateTimeRange(targetTime, planSet).not() -> {
                (context as BaseActivity).showToast("任务重叠，请选择合适的时间段")
                return false
            }
        }
        return true
    }

}