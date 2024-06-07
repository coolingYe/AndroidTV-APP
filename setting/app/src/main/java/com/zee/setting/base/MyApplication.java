package com.zee.setting.base;

import android.app.Application;

import com.zee.setting.utils.RemoteManage;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        DiskCacheManager.init(this);
        RemoteManage.initialize(this);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
       // SPUtils.getInstance().put(SharePrefer.Gesture,"close");
    }
}
