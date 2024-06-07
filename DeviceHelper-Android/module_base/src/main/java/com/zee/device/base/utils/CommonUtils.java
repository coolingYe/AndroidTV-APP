package com.zee.device.base.utils;

import android.content.Context;
import android.os.Build;
import android.os.PowerManager;
import android.os.SystemClock;
import android.provider.Settings;
import android.view.View;

import androidx.core.view.ViewCompat;

import com.zee.device.base.config.BaseConstants;

import java.io.IOException;
import java.lang.reflect.Method;

public class CommonUtils {

    public static String getFileUsePath(String fileId, String version, int type, Context context) {
        if (type == BaseConstants.DownloadFileType.HOST_APP) {
            return context.getExternalCacheDir().getPath()+ "/" + fileId + "_" + version + ".apk";
        } else {
            return fileId;
        }
    }


    public static boolean deviceGoToSleep(Context context) {
        PowerManager pm = (PowerManager) context.getApplicationContext().getSystemService(Context.POWER_SERVICE);
        try {
            Method method = pm.getClass().getMethod("goToSleep", Long.TYPE);
            method.invoke(pm, SystemClock.uptimeMillis());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void scaleView(View view, float scale) {
        ViewCompat.animate(view)
                .setDuration(200)
                .scaleX(scale)
                .scaleY(scale)
                .start();
    }

    public static boolean deviceWakeUp(Context context) {
        PowerManager pm = (PowerManager) context.getApplicationContext().getSystemService(Context.POWER_SERVICE);
        try {
            Method method = pm.getClass().getMethod("wakeUp", Long.TYPE);
            method.invoke(pm, SystemClock.uptimeMillis());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static Process clearAppUserData(String packageName) {
        /*
         ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
                if (am != null) {
                    boolean ret = am.clearApplicationUserData();
                    Log.w(TAG, "Clear application user data result:" + ret);
                }
         */
        Process p = execRuntimeProcess("pm clear " + packageName);
        return p;
    }

    public static Process execRuntimeProcess(String commond) {
        Process p = null;
        try {
            p = Runtime.getRuntime().exec(commond);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return p;
    }

    public static String getDeviceName(Context context) {
        String deviceName = null;
        try {
            deviceName = Settings.Secure.getString(context.getContentResolver(), "bluetooth_name");

            if (deviceName == null || deviceName.isEmpty()) {
                deviceName = Settings.Secure.getString(context.getContentResolver(), "device_name");
            }

            if (deviceName == null || deviceName.isEmpty()) {
                return Build.MODEL;
            } else {
                return deviceName;
            }
        } catch (Exception e) {
            return "手机摄像头";
        }
    }
}
