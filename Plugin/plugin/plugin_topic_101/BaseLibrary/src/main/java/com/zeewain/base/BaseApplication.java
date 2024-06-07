package com.zeewain.base;

import android.app.Application;
import android.content.Context;
import android.os.Build;

import com.zeewain.base.utils.ApkUtil;
import com.zeewain.base.utils.CommonUtils;
import com.zeewain.base.utils.DiskCacheManager;
import com.zeewain.base.utils.ToastUtils;
import com.zwn.launcher.host.HostManager;

public class BaseApplication extends Application {
    public static Context applicationContext;
    public static String platformInfo = null;
    public static String userToken = null;
    public static String baseUrl = null;
    public static String basePath = null;
    public static String deviceSn = null;
    public static boolean isPlayingVideo = false;
    public static boolean isVidePlayed = false;

    @Override
    public void onCreate() {
        super.onCreate();
        applicationContext = this;
        ToastUtils.init(this);
        DiskCacheManager.init(this);
    }

    //AndroidTVAIIP/1.0.000 (ZWN_AIIP_001 1.0; Android 9.***)
    public String buildPlatformInfo(){
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("AndroidTVAIIP/")
                .append(ApkUtil.getAppVersionName(getApplicationContext()))
                .append(" (ZWN_AIIP_003 1.0; Android ")
                .append(Build.VERSION.RELEASE)
                .append(")");
        return stringBuffer.toString();
    }

    public static synchronized void handleUnauthorized(){
        HostManager.logoutClear();//userToken,userAccount,akSkInfo
        userToken = null;
        if(!isPlayingVideo)
            CommonUtils.toPluginMainActivity(applicationContext, false);
    }

}
