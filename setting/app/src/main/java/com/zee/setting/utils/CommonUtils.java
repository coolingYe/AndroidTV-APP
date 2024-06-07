package com.zee.setting.utils;


import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.view.View;

import androidx.core.view.ViewCompat;

import com.zee.setting.base.BaseConstants;
import com.zee.setting.config.Config;

import java.util.List;

import com.zee.setting.base.BaseConstants;

public class CommonUtils {

    public static final String TAG = "CommonUtils";
    private static final String SMART_LAUNCHER_APP_PACKAGE_NAME = "com.zee.launcher";
    public static final String SMART_LAUNCHER_SP_FILE_NAME = "spUtils";
    public static final String AK_SK_INFO = "AkSkInfo";
    public static final String BASE_URL = "BaseUrl";
    public static final String BASE_PATH = "BasePath";
    private static final String SETTING_APP_PACKAGE_NAME = "com.zee.setting";
    private static final String SETTING_SP_FILE_NAME = "spUtils";
    public static final String Camera = "camera";

    public static String getDeviceSn(){
        //return "HD13213213223213213";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            return Build.getSerial();
        } else {
            return SystemProperties.get("ro.serialno");
        }
    }

    public static boolean isCarePrivateData(String data){
        return "970719cb674e0f9af2450bae18c19cb9".equalsIgnoreCase(MD5Util.string2MD5(data));
    }

    public static boolean isEnablePrivateFun(String data){
        return "4a889ba78563a477a4754fc52a54e6fc".equals(MD5Util.string2MD5(data));
    }

    public static void startSettings(Context context){
        Intent intent = new Intent(Settings.ACTION_SETTINGS);
        context.startActivity(intent);
    }

    public static void startSettingsActivity(Context context){
        try {
            Intent intent = new Intent();
            intent.setPackage(context.getPackageName());
            intent.setAction(BaseConstants.ZEE_SETTINGS_ACTIVITY_ACTION);
            intent.putExtra(BaseConstants.EXTRA_CAMERA_EVENT, true);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void saveGlobalCameraId(String cameraId) {
        SystemProperties.set(Config.PERSIST_DATA_SPECIFY_CAMERA_ID, cameraId);
    }

    public static String queryOnTopRecentTasksPkg(Context context){
        try {
            ActivityManager am = (ActivityManager) context.getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RecentTaskInfo> recentTaskList = am.getRecentTasks(Integer.MAX_VALUE, ActivityManager.LOCK_TASK_MODE_NONE);
            if (recentTaskList !=null && recentTaskList.size() > 0) {
                ActivityManager.RecentTaskInfo recentTaskInfo = recentTaskList.get(0);
                Intent intent = new Intent(recentTaskInfo.baseIntent);
                if (recentTaskInfo.origActivity != null) {
                    intent.setComponent(recentTaskInfo.origActivity);
                }
                return intent.getComponent().getPackageName();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void scaleView(View view, float scale){
        ViewCompat.animate(view)
                .setDuration(200)
                .scaleX(scale)
                .scaleY(scale)
                .start();
    }

    //Get the BaseUrl from the com.zee.launcher.
    public static String getBaseUrl(Context context) {
        try {
            Context otherAppContext = context.createPackageContext("com.zee.launcher", Context.CONTEXT_IGNORE_SECURITY);
            SharedPreferences sharedPreferences = otherAppContext.getSharedPreferences("spUtils", Context.MODE_PRIVATE);
            return sharedPreferences.getString("BaseUrl", null);
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }
}
