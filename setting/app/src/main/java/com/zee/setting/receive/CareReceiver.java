package com.zee.setting.receive;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.zee.setting.service.ConnectService;
import com.zee.setting.utils.Logger;

public class CareReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Logger.i("CareReceiver", "onReceive() " + intent);
        ConnectService.initConnectService(context);
    }
}
