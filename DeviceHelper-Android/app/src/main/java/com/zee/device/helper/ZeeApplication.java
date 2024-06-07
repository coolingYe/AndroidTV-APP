package com.zee.device.helper;

import android.app.Application;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;

import com.zee.device.base.db.DatabaseController;
import com.zee.device.base.utils.ToastUtils;
import com.zee.device.helper.ui.main.MainActivity;
import com.zee.wireless.camera.service.CallService;
import com.zwn.lib_download.db.CareController;

public class ZeeApplication extends Application {
    public static ZeeApplication instance;
    public CallService mService;
    public ServiceConnection mServiceConnection;
    public Context mainContext;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        DatabaseController.init(this);
        CareController.init(this);
        ToastUtils.init(getApplicationContext());
    }


    public static ZeeApplication getInstance() {
        return instance;
    }

    public synchronized void unBindService() {
        if (mServiceConnection != null) {
            mService.stopAll();
            mService.stopForeground(true);
            mService.stopSelf();
            unbindService(mServiceConnection);
            ((MainActivity) mainContext).onExistCallActivity();
            mainContext = null;
            mServiceConnection = null;
            mService = null;
        }
    }

    public void bindGService(Context context) {
        Intent socketIntent = new Intent(instance, CallService.class);
        if (mServiceConnection != null) {
            mainContext = context;
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                instance.startForegroundService(socketIntent);
//            }
            instance.bindService(socketIntent, mServiceConnection, Service.BIND_AUTO_CREATE);
        }
    }
}
