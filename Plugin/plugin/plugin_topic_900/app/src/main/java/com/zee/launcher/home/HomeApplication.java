package com.zee.launcher.home;

import android.content.Context;


import com.zeewain.base.BaseApplication;
import com.zeewain.base.config.SharePrefer;
import com.zeewain.base.utils.FontUtils;
import com.zwn.launcher.host.HostManager;

public class HomeApplication extends BaseApplication {


    @Override
    public void onCreate() {
        super.onCreate();
        initHostData();
        FontUtils.initAssetFont(this, "fonts/mmd.ttf");
    }

    public static void initHostData(){
        userToken = HostManager.getHostSpString(SharePrefer.userToken, null);
        platformInfo = HostManager.getHostSpString(SharePrefer.platformInfo, null);
        baseUrl = HostManager.getHostSpString(SharePrefer.baseUrl, null);
        basePath = HostManager.getHostSpString(SharePrefer.basePath, null);
        deviceSn = HostManager.getHostSpString(SharePrefer.DeviceSN, null);
    }
}
