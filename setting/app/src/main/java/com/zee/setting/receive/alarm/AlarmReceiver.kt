package com.zee.setting.receive.alarm

import android.app.AlarmManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.zee.setting.BuildConfig
import com.zee.setting.utils.SystemUtils
import com.zee.setting.utils.ToastUtils

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (intent.action.equals(AlarmSetter.ACTION_TASK)) {
            val id = intent.getIntExtra(AlarmSetter.ACTION_ID, -1)
            when (val type = intent.getStringExtra(AlarmSetter.ACTION_TASK_TYPE)) {
                AlarmSetter.ACTION_SYSTEM_SLEEP -> {
                    Log.d("Test---->Sleep $id", "Success")
                    SystemUtils.sleepSystem(context)
                    AlarmSetter.AlarmSetterImpl(mContext = context, am = alarmManager).setUpRTCAlarm(
                            id,
                            type,
                            System.currentTimeMillis() + AlarmSetter.AlarmUtil.getIntervalMillis()
                        )
                }
                AlarmSetter.ACTION_SYSTEM_WAKE_UP -> {
                    Log.d("Test---->WakeUp $id", "Success")
                    SystemUtils.wakeUpSystem(context)
                    AlarmSetter.AlarmSetterImpl(mContext = context, am = alarmManager).setUpRTCAlarm(
                            id,
                            type,
                            System.currentTimeMillis() + AlarmSetter.AlarmUtil.getIntervalMillis()
                        )
                }
            }
        }
    }

}