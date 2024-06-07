package com.zeewain.base.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import java.util.List;


/**
 * Created by JuAn_Zhangsongzhou on 2017/5/5.
 * 获取app版本信息
 * <p>
 * build.gradle
 */

public class AppVersionUtil {

    private static final String TAG = "AppVersionUtil";
   public static boolean isFirst=false;

    /**
     * 获取当前程序版本名
     *
     * @param context
     * @return
     */
    public static String getAppVersionName(Context context) {
        PackageManager manager = context.getPackageManager();
        String versionName = "";
        try {
            versionName = manager.getPackageInfo(context.getPackageName(), PackageManager.GET_CONFIGURATIONS).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return versionName;
    }

    public static String getAppVersionName(Context context,String packageName) {
        PackageManager manager = context.getPackageManager();
        String versionName = "";
        try {
            versionName = manager.getPackageInfo(packageName, PackageManager.GET_CONFIGURATIONS).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return versionName;
    }

    /**
     * 比较当前版本是否在指定版本之后（包含指定版本）
     * @param currentAppVersion 当前版本
     * @param compareVersion 指定比较的版本
     * @return
     */
    public static boolean isAfterVersion(String currentAppVersion, String compareVersion) {
        try {
            // 有效比较位
            int validNum = 3;
            if (currentAppVersion.contains(".") && compareVersion.contains(".")) {
                String[] currentAppVersionSplit = currentAppVersion.split("\\.");
                String[] compareVersionSplit = compareVersion.split("\\.");
                if (currentAppVersionSplit.length >= validNum && compareVersionSplit.length >= validNum) {
                    for (int i = 0; i < validNum; i++) {
                        if (Integer.valueOf(currentAppVersionSplit[i]) < Integer.valueOf(compareVersionSplit[i])) {
                            return false;
                        }
                    }
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 获取程序版本号
     *
     * @param context
     * @return
     */
    public static int getAppVersionCode(Context context) {
        int versionCode = -1;
        try {
            versionCode = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return versionCode;
    }

    /**
     * 获取当前程序包名
     *
     * @param context
     * @return
     */
    public static String getAppPackageName(Context context) {
        String packageName = "";
        try {
            packageName = context.getPackageName();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return packageName;
    }


    /**
     * 获取当前程序UID
     *
     * @param context
     * @return
     */
    public static int getAppUID(Context context) {
        int uid = -1;
        PackageManager pm = context.getPackageManager();
        try {
            ApplicationInfo ai = pm.getApplicationInfo(getAppPackageName(context), PackageManager.GET_ACTIVITIES);
            uid = ai.uid;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return uid;
    }
    public static String getUID(Context context) {
        SharedPreferences preference = context.getSharedPreferences("UID", Context.MODE_PRIVATE);
        String uid = preference.getString("phoneUid", null);
        if (uid == null) {
            uid = java.util.UUID.randomUUID().toString();
            preference.edit().putString("phoneUid", uid).commit();
        }
        return uid;
    }

    /**
     * 获取当前应用名称
     *
     * @param context
     * @return
     */
    public static String getAppName(Context context) {
        String appName = "";
        PackageManager packageManager = context.getPackageManager();
        try {
            appName = packageManager.getApplicationLabel(packageManager.getApplicationInfo(getAppPackageName(context), PackageManager.GET_ACTIVITIES)).toString();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return appName;
    }


    public static boolean isForeground(Context context) {
        ActivityManager am =
                (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> lr = am.getRunningAppProcesses();
        if (lr == null) {
            return false;
        }
        String packageName = getAppPackageName(context);
        for (ActivityManager.RunningAppProcessInfo ra : lr) {
            if (ra.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_VISIBLE
                    || ra.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                if (packageName.equals(ra.processName)) {
                    return true;
                }
            }
        }
        return false;
    }
    /**
     * 判断当前应用是否是debug状态
     */

    public static boolean isApkInDebug(Context context) {
        try {
            ApplicationInfo info = context.getApplicationInfo();
            return (info.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
        } catch (Exception e) {
            return false;
        }
    }
}
