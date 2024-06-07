package com.zwn.launcher.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.zeewain.base.config.SharePrefer;
import com.zeewain.base.utils.CareLog;
import com.zeewain.base.utils.SPUtils;

public class CareReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        CareLog.i("CareReceiver", "onReceive() " + intent);
        SPUtils.getInstance().put(SharePrefer.BootCompleted, true);
    }
}
