package com.zee.setting.receive.alarm

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.net.ParseException
import com.zee.setting.base.BaseActivity
import com.zee.setting.base.BaseConstants
import com.zee.setting.cache.SPUtils
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

interface AlarmSetter {
    fun removeRTCAlarm(id: Int)
    fun setUpRTCAlarm(id: Int, actionType: String, calendar: Long)

    class AlarmSetterImpl(
        private val am: AlarmManager,
        private val mContext: Context
    ) : AlarmSetter {

        override fun removeRTCAlarm(id: Int) {
            val pendingIntent = Intent(ACTION_TASK).apply {
                setClass(mContext, AlarmReceiver::class.java)
                putExtra(ACTION_ID, id)
            }.let {
                PendingIntent.getBroadcast(
                    mContext, id, it, pendingIntentUpdateCurrentFlag()
                )
            }
            am.cancel(pendingIntent)
        }

        override fun setUpRTCAlarm(id: Int, actionType: String, calendar: Long) {
            val pendingIntent = Intent(ACTION_TASK).apply {
                setClass(mContext, AlarmReceiver::class.java)
                putExtra(ACTION_ID, id)
                putExtra(ACTION_TASK_TYPE, actionType)
            }.let {
                PendingIntent.getBroadcast(
                    mContext, id, it, pendingIntentUpdateCurrentFlag()
                )
            }
            am.setExact(AlarmManager.RTC_WAKEUP, calendar, pendingIntent)
        }
    }

    fun pendingIntentUpdateCurrentFlag(): Int {
        return if (Build.VERSION.SDK_INT >= 31) {
            PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
    }

    object AlarmUtil {

        fun getTargetCalendar(targetTime: String): Long {
            val hour = targetTime.substringBefore(":").toInt()
            val minute = targetTime.substringAfter(":").toInt()
            val calendarTarget = Calendar.getInstance()
            calendarTarget.timeInMillis = System.currentTimeMillis()
            calendarTarget.set(Calendar.HOUR_OF_DAY, hour)
            calendarTarget.set(Calendar.MINUTE, minute)
            calendarTarget.set(Calendar.SECOND, 0)
            calendarTarget.set(Calendar.MILLISECOND, 0)

            val calendarCurrent = Calendar.getInstance()
            calendarCurrent.timeInMillis = System.currentTimeMillis()

            //当目标时间小于当前时间时设置成第二天的闹钟
            if (calendarTarget.before(calendarCurrent)) {
                calendarTarget.add(Calendar.DATE, 1)
            }

            return calendarTarget.timeInMillis
        }

        fun openAllAlarmTask (context: Context) {
            val alarmManager = context.getSystemService(BaseActivity.ALARM_SERVICE) as AlarmManager
            val taskList = SPUtils.getInstance().getStringSet(BaseConstants.SP_KEY_TIMER_PLAN)
            if (taskList.isEmpty()) return
            taskList.forEach(action = { targetTime ->
                val startTime = targetTime.substringBefore("~")
                val endTime = targetTime.substringAfter("~")
                // System Sleep Task
                AlarmSetterImpl(am = alarmManager, mContext = context)
                    .setUpRTCAlarm(startTime.hashCode(), ACTION_SYSTEM_SLEEP, getTargetCalendar(startTime))

                // System Wake Up Task
                AlarmSetterImpl(am = alarmManager, mContext = context)
                    .setUpRTCAlarm(endTime.hashCode(), ACTION_SYSTEM_WAKE_UP, getTargetCalendar(endTime))
            })
        }

        fun stopAllAlarmTask (context: Context) {
            val alarmManager = context.getSystemService(BaseActivity.ALARM_SERVICE) as AlarmManager
            val taskList = SPUtils.getInstance().getStringSet(BaseConstants.SP_KEY_TIMER_PLAN)
            if (taskList.isEmpty()) return
            taskList.forEach(action = { targetTime ->
                val startTime = targetTime.substringBefore("~")
                val endTime = targetTime.substringAfter("~")
                AlarmSetterImpl(am = alarmManager, mContext = context)
                    .removeRTCAlarm(startTime.hashCode())
                AlarmSetterImpl(am = alarmManager, mContext = context)
                    .removeRTCAlarm(endTime.hashCode())
            })
        }

        fun getIntervalMillis(): Long = (24 * 3600 * 1000).toLong()

        fun validateTimeRange(targetRange: String, existingRanges: MutableSet<String>): Boolean {
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            sdf.timeZone = TimeZone.getTimeZone("UTC")

            // 解析目标时间段
            val targetTimes = targetRange.split("~")
            val targetStartTimeMillis = try {
                sdf.parse(targetTimes[0].trim())?.time ?: 0
            } catch (e: ParseException) {
                return false
            }
            val targetEndTimeMillis = try {
                sdf.parse(targetTimes[1].trim())?.time ?: 0
            } catch (e: ParseException) {
                return false
            }

            // 验证目标时间段与现有时间段之间的交集或重叠
            for (range in existingRanges) {
                val times = range.split("~")
                val startTimeMillis = try {
                    sdf.parse(times[0].trim())?.time ?: 0
                } catch (e: ParseException) {
                    continue
                }
                val endTimeMillis = try {
                    sdf.parse(times[1].trim())?.time ?: 0
                } catch (e: ParseException) {
                    continue
                }

                if (startTimeMillis >= targetEndTimeMillis || endTimeMillis <= targetStartTimeMillis) {
                    // 没有交集或重叠
                    continue
                }

                // 有交集或重叠
                return false
            }

            return true
        }
    }

    companion object {
        @SuppressLint("SimpleDateFormat")
        val dateFormat: DateFormat = SimpleDateFormat("HH:mm")

        const val ACTION_ID = "ACTION_ID"
        const val ACTION_TASK_TYPE = "ACTION_TASK_TYPE"
        const val ACTION_TASK = "ACTION_TASK"
        const val ACTION_SYSTEM_SLEEP = "ACTION_SYSTEM_SLEEP"
        const val ACTION_SYSTEM_WAKE_UP = "ACTION_SYSTEM_WAKE_UP"
    }
}