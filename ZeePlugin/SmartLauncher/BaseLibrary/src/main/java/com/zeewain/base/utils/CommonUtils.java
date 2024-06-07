package com.zeewain.base.utils;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.SystemClock;
import android.provider.Settings;
import android.view.View;

import androidx.annotation.RequiresApi;
import androidx.core.view.ViewCompat;
import com.zeewain.base.BaseApplication;
import com.zeewain.base.BuildConfig;
import com.zeewain.base.config.BaseConstants;
import com.zeewain.base.config.SharePrefer;
import com.zeewain.base.data.protocol.response.UpgradeResp;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.List;

public class CommonUtils {

    public static String getFileUsePath(String fileId, String version, int type, Context context){
        return getFileUsePath(fileId, version, "", type, context);
    }

    public static String getFileUsePath(String fileId, String version, String fileName, int type, Context context){
        if(type == BaseConstants.DownloadFileType.HOST_APP){
            return BaseConstants.PRIVATE_DATA_PATH + "/" + fileId + "_" + version + ".apk";
        }else if(type == BaseConstants.DownloadFileType.MANAGER_APP
                || type == BaseConstants.DownloadFileType.SETTINGS_APP
                || type == BaseConstants.DownloadFileType.ZEE_GESTURE_AI_APP){
            return BaseConstants.PRIVATE_DATA_PATH + "/" + fileId + "_" + version + ".apk";
        }else if(type == BaseConstants.DownloadFileType.PLUGIN_APP){
            return context.getExternalCacheDir().getPath() + "/" + fileId + "_" + version + ".apk";
        }else if(type == BaseConstants.DownloadFileType.HOST_PLUGIN_PKG){
            return context.getFilesDir().getPath() + "/" + fileId + "_" + version + ".apk";
        }else if(type == BaseConstants.DownloadFileType.SHARE_LIB){
            return context.getFilesDir().getPath() + "/" + fileId + "_" + version + ".zip";
        }else if(type == BaseConstants.DownloadFileType.MODEL_BIN){
            return getModelStorePath(fileName, context);
        }else{
            if(fileName != null && !fileName.isEmpty()){
                return BaseConstants.PRIVATE_DATA_PATH + "/" + fileName;
            }
            return BaseConstants.PRIVATE_DATA_PATH + "/" + fileId + "_" + version;
        }
    }

    public static String getModelStorePath(String modelFileName, Context context){
        String path = context.getFilesDir().getPath() + "/models";
        File file = new File(path);
        if(!file.exists()){
            file.mkdirs();
        }
        return path + "/" + modelFileName;
    }

    public static boolean createPrivateDir(){
        File file = new File(BaseConstants.PRIVATE_DATA_PATH);
        if(!file.exists()){
            return file.mkdirs();
        }
        return true;
    }

    public static boolean createOrClearPluginModelDir(){
        File file = new File(BaseConstants.PLUGIN_MODEL_PATH);
        if(!file.exists()){
            return file.mkdirs();
        }else {
            File[] files = file.listFiles();
            if(files != null){
                for(File tmpFile : files){
                    if (tmpFile.isFile()){
                        tmpFile.delete();
                    }
                }
            }
        }
        return true;
    }

    public static boolean isCarePrivateData(String data){
        return BaseConstants.PRIVATE_DATA_CARE_CODE.equalsIgnoreCase(MD5Util.string2MD5(data));
    }

    public static void startSettings(Context context){
        Intent intent = new Intent(Settings.ACTION_SETTINGS);
        context.startActivity(intent);
    }

    public static void setPersistData(){
        SystemProperties.set(BaseConstants.PERSIST_DATA_STATUS_BAR, "false");
        SystemProperties.set(BaseConstants.PERSIST_DATA_MENU_SETTINGS, "com.zee.setting/com.zee.setting.activity.SettingActivity");
    }

    public static boolean isDebugEnable(){
        if(BuildConfig.DEBUG) return true;
        return BaseConstants.PRIVATE_DATA_DEBUG_CODE.equalsIgnoreCase(MD5Util.string2MD5(SystemProperties.get(BaseConstants.PERSIST_DATA_DEBUG_STATUS, "")));
    }

    public static boolean isUserLogin(){
        String userToken = SPUtils.getInstance().getString(SharePrefer.userToken);
        if(userToken != null && !userToken.isEmpty()){
            String akSkInfo = SPUtils.getInstance().getString(SharePrefer.akSkInfo);
            return (akSkInfo != null && !akSkInfo.isEmpty());
        }
        return false;
    }

    public static void savePluginCareInfo(){
        SPUtils.getInstance().put(SharePrefer.platformInfo, BaseApplication.platformInfo);
        SPUtils.getInstance().put(SharePrefer.baseUrl, BaseConstants.baseUrl);
        SPUtils.getInstance().put(SharePrefer.basePath, BaseConstants.basePath);
        SPUtils.getInstance().put(SharePrefer.DeviceSN, CommonUtils.getDeviceSn());

        SystemProperties.set(BaseConstants.PERSIST_DATA_DEVICE_SERIAL_NO, getDeviceSn());
    }

    public static void logoutClear(){
        SPUtils.getInstance().remove(SharePrefer.userToken);
        SPUtils.getInstance().remove(SharePrefer.userAccount);
        SPUtils.getInstance().remove(SharePrefer.akSkInfo);
    }

    public static void scaleView(View view, float scale){
        ViewCompat.animate(view)
                .setDuration(200)
                .scaleX(scale)
                .scaleY(scale)
                .start();
    }

    public static String getDeviceSn(){
        //return "HD13213213223213213";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            return Build.getSerial();
        } else {
            return SystemProperties.get("ro.serialno");
        }
    }

    public static String getCrashModuleInfo(){
        return getDeviceSn() + "_" + BaseApplication.pluginBaseInfo;
    }

    public static String getHardwarePlatformInfo(){
        if("amlogic".equals(Build.HARDWARE)){
            return "AndroidAmlogicTVAIIP";
        }else if("rk30board".equals(Build.HARDWARE)){
            return "AndroidRockchipTVAIIP";
        }
        return "AndroidTVAIIP";
    }

    public static void startSettingsActivity(Context context){
        try {
            Intent intent = new Intent();
            intent.setAction(BaseConstants.ZEE_SETTINGS_ACTIVITY_ACTION);
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean startMainActivity(Context context){
        try {
            SPUtils.getInstance().put(SharePrefer.GuideDone, true);
            Intent intent = new Intent(context, Class.forName(BaseConstants.MAIN_PKG_CLASS_NAME));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            context.startActivity(intent);
        } catch (ClassNotFoundException e) {
            return false;
        }
        return true;
    }

    public static boolean startUpgradeActivity(Context context, UpgradeResp upgradeResp){
        try {
            Intent intent = new Intent(context, Class.forName(BaseConstants.UPGRADE_PKG_CLASS_NAME));
            Bundle bundle = new Bundle();
            bundle.putSerializable(BaseConstants.EXTRA_UPGRADE_INFO, upgradeResp);
            intent.putExtra(BaseConstants.EXTRA_UPGRADE_INFO, bundle);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            context.startActivity(intent);
        } catch (ClassNotFoundException e) {
            return false;
        }
        return true;
    }

    public static boolean startGuideActivity(Context context, boolean clearAllCache){
        try {
            Intent intent = new Intent(context, Class.forName(BaseConstants.GUIDE_PKG_CLASS_NAME));
            intent.putExtra(BaseConstants.EXTRA_CLEAR_ALL_CACHE, clearAllCache);
            context.startActivity(intent);
        } catch (ClassNotFoundException e) {
            return false;
        }
        return true;
    }

    public static void runCmdByManagerActivity(Context context, String loadingTip, String cmdStr){
        Intent intent = new Intent();
        ComponentName componentName = new ComponentName(BaseConstants.MANAGER_PACKAGE_NAME, BaseConstants.MANAGER_MANAGER_ACTIVITY);
        intent.setComponent(componentName);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(BaseConstants.EXTRA_MANAGER_LOADING_TIP, loadingTip);
        intent.putExtra(BaseConstants.EXTRA_MANAGER_ACTION, 1000);
        intent.putExtra(BaseConstants.EXTRA_MANAGER_ACTION_PWD, "Zee!@#$%^123_._456.");
        intent.putExtra(BaseConstants.EXTRA_MANAGER_RUN_COMMAND, cmdStr);
        intent.putExtra(BaseConstants.EXTRA_DONE_TO_PACKAGE_NAME, context.getPackageName());
        intent.putExtra(BaseConstants.EXTRA_DONE_TO_CLASS_NAME, BaseConstants.DONE_TO_CLASS_NAME);
        context.startActivity(intent);
    }

    public static int getCameraNum(Context context) {
        CameraManager cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        try {
            String[] cameraIds = cameraManager.getCameraIdList();
            if (cameraIds != null) {
                return cameraIds.length;
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        return 0;
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

                if (intent.getComponent() != null) {
                    return intent.getComponent().getPackageName();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public static long[] getMemFreeAndAvailableInfo() {
        BufferedReader bufferedReader = null;
        try {
            Process process = Runtime.getRuntime().exec("cat /proc/meminfo");
            bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String lineStr = "";
            long[] memInfoData = new long[]{0L, 0L, 0L};
            while ((lineStr = bufferedReader.readLine()) != null) {
                if (lineStr.startsWith("MemTotal:")) {
                    String memFree = lineStr.replace("MemTotal:", "").replace("kB", "");
                    memInfoData[0] = Long.parseLong(memFree.trim());
                } else if (lineStr.startsWith("MemFree:")) {
                    String memFree = lineStr.replace("MemFree:", "").replace("kB", "");
                    memInfoData[1] = Long.parseLong(memFree.trim());
                } else if (lineStr.startsWith("MemAvailable:")) {
                    String memAvailable = lineStr.replace("MemAvailable:", "").replace("kB", "");
                    memInfoData[2] = Long.parseLong(memAvailable.trim());
                }

                if (memInfoData[0] > 0 && memInfoData[1] > 0 && memInfoData[2] > 0) {
                    break;
                }
            }
            return memInfoData;
        } catch (Exception e) {
            CareLog.e("getMemFreeAndAvailableInfo() err " + e);
        } finally {
            if(bufferedReader != null){
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    CareLog.e("getMemFreeAndAvailableInfo() err " + e);
                }
            }
        }
        return new long[]{0L, 0L, 0L};
    }

    public static boolean execRuntimeCommand(String command) {
        BufferedReader bufferedReader = null;
        try {
            CareLog.e("execRuntimeCommand() " + command);
            Process process = Runtime.getRuntime().exec(command);
            bufferedReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String errMsg = bufferedReader.readLine();
            if(errMsg != null){
                CareLog.e("errResult-->" + errMsg);
                return false;
            }
            return true;
        } catch (Exception e) {
            CareLog.e("execRuntimeCommand() err " + e);
        } finally {
            if(bufferedReader != null){
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    CareLog.e("execRuntimeCommand() err " + e);
                }
            }
        }
        return false;
    }


}
