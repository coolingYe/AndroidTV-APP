package com.zee.launcher.home;

import android.app.Service;
import android.content.Intent;
import android.content.ServiceConnection;
import android.util.Log;

import com.zee.launcher.home.service.GestureCameraService;
import com.zeewain.base.BaseApplication;
import com.zeewain.base.config.SharePrefer;
import com.zwn.launcher.host.HostManager;

public class HomeApplication extends BaseApplication {

    public static HomeApplication instance;
    public GestureCameraService gestureCameraService;
    public ServiceConnection serviceConnection;
    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        initHostData();
    }

    public static void initHostData() {
        userToken = HostManager.getHostSpString(SharePrefer.userToken, null);
        platformInfo = HostManager.getHostSpString(SharePrefer.platformInfo, null);
        baseUrl = HostManager.getHostSpString(SharePrefer.baseUrl, null);
        basePath = HostManager.getHostSpString(SharePrefer.basePath, null);
        deviceSn = HostManager.getHostSpString(SharePrefer.DeviceSN, null);
    }

    public static HomeApplication getInstance() {
        return instance;
    }

    public void unBindService() {
        if (serviceConnection != null) {
            unbindService(serviceConnection);
            serviceConnection = null;
            gestureCameraService = null;
        }
    }

    public void bindGService()
    {
        Intent socketInten = new Intent(HomeApplication.getInstance(), GestureCameraService.class);
        HomeApplication.getInstance().bindService(socketInten, HomeApplication.getInstance().serviceConnection, Service.BIND_AUTO_CREATE);
    }
}
