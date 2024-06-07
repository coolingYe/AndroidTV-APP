package com.zeewain.base;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.zeewain.base.config.BaseConstants;
import com.zeewain.base.config.SharePrefer;
import com.zeewain.base.data.protocol.response.UpgradeResp;
import com.zeewain.base.utils.ApkUtil;
import com.zeewain.base.utils.CommonUtils;
import com.zeewain.base.utils.DiskCacheManager;
import com.zeewain.base.utils.SPUtils;
import com.zeewain.base.utils.ToastUtils;

public class BaseApplication extends Application {
    public static Context applicationContext;
    public static String platformInfo = null;
    public static String pluginBaseInfo = "";// for crash log;

    @Override
    public void onCreate() {
        super.onCreate();
        ToastUtils.init(this);
        DiskCacheManager.init(this);
        platformInfo = buildPlatformInfo();
    }

    //AndroidTVAIIP/1.0.000 (ZWN_AIIP_001 1.0; Android 9.***)
    public String buildPlatformInfo(){
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(CommonUtils.getHardwarePlatformInfo()).append("/")
                .append(ApkUtil.getAppVersionName(getApplicationContext()))
                .append(" (")
                .append(BaseConstants.AUTH_SYSTEM_CODE)
                .append(" 1.0; Android ")
                .append(Build.VERSION.RELEASE)
                .append(")");
        return stringBuffer.toString();
    }

    public static synchronized void handleUnauthorized(){
        try {
            boolean topicLogin = SPUtils.getInstance().getBoolean(SharePrefer.TopicLogin, false);
            if(!topicLogin) {
                CommonUtils.logoutClear();
                Intent intent = new Intent(applicationContext, Class.forName(BaseConstants.LOGIN_PKG_CLASS_NAME));
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                applicationContext.startActivity(intent);
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void handleManagerUpgrade(UpgradeResp upgradeResp){
        if(mainModuleService != null){
            mainModuleService.handleManagerUpgrade(upgradeResp);
        }
    }

    public static MainModuleService mainModuleService;
    public interface MainModuleService{
        void handleManagerUpgrade(UpgradeResp upgradeResp);
    }

}
