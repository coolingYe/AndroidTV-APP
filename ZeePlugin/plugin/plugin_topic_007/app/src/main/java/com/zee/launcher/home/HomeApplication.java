package com.zee.launcher.home;

import android.content.Context;

import com.danikula.videocache.HttpProxyCacheServer;
import com.zee.launcher.home.service.TopicService;
import com.zeewain.base.BaseApplication;
import com.zeewain.base.config.BaseConstants;
import com.zeewain.base.config.SharePrefer;
import com.zeewain.base.utils.FontUtils;
import com.zeewain.base.utils.SystemProperties;
import com.zwn.launcher.host.HostManager;

public class HomeApplication extends BaseApplication {
    private HttpProxyCacheServer proxy;

    @Override
    public void onCreate() {
        super.onCreate();
        initHostData();
        FontUtils.initAssetFont(this, "fonts/mmd.ttf");
        TopicService.initTopicService(this);
        SystemProperties.set(BaseConstants.SP_KEY_CAMERA_FUNCTION_SWITCH, "true");
    }

    public static void initHostData(){
        userToken = HostManager.getHostSpString(SharePrefer.userToken, null);
        platformInfo = HostManager.getHostSpString(SharePrefer.platformInfo, null);
        baseUrl = HostManager.getHostSpString(SharePrefer.baseUrl, null);
        basePath = HostManager.getHostSpString(SharePrefer.basePath, null);
        deviceSn = HostManager.getHostSpString(SharePrefer.DeviceSN, null);
    }

    public static HttpProxyCacheServer getProxy(Context context) {
        HomeApplication app = (HomeApplication) context.getApplicationContext();
        return app.proxy == null ? (app.proxy = app.newProxy()) : app.proxy;
    }

    private HttpProxyCacheServer newProxy() {
        return new HttpProxyCacheServer(this);
    }
}
