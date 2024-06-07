package com.zeewain.base.utils;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.view.View;

import androidx.core.view.ViewCompat;

import com.zeewain.base.BaseApplication;
import com.zeewain.base.BuildConfig;
import com.zeewain.base.config.BaseConstants;

import java.io.File;

public class CommonUtils {
    private static String logoUrl = "";

    public static String getFileUsePath(String fileId, String version, int type, Context context){
        if(type == BaseConstants.DownloadFileType.HOST_APP){
            return BaseConstants.PRIVATE_DATA_PATH + "/" + fileId + "_" + version + ".apk";
        }else if(type == BaseConstants.DownloadFileType.MANAGER_APP){
            return BaseConstants.PRIVATE_DATA_PATH + "/" + fileId + "_" + version + ".apk";
        }else if(type == BaseConstants.DownloadFileType.PLUGIN_APP){
            return context.getExternalCacheDir().getPath() + "/" + fileId + "_" + version + ".apk";
        }else if(type == BaseConstants.DownloadFileType.SHARE_LIB){
            return context.getFilesDir().getPath() + "/" + fileId + "_" + version + ".zip";
        }else{
            return fileId;
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

    public static boolean createOrClearPluginModelDir(){
        File file = new File(BaseConstants.PLUGIN_MODEL_PATH);
        if(!file.exists()){
            return file.mkdirs();
        }else {
            File[] files = file.listFiles();
            for(File tmpFile : files){
                if (tmpFile.isFile()){
                    tmpFile.delete();
                }
            }
        }
        return true;
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

    public static String getThemeName(String themeName, String defaultValue) {
        String name = themeName;
        if (name.length() == 0) {
            name = defaultValue;
        }

        if (name.length() > 14) {
            return name.substring(0, 14) + "...";
        }
        return name;
    }

    public static boolean isDebugEnable(){
        if(BuildConfig.DEBUG) return true;
        return BaseConstants.PRIVATE_DATA_DEBUG_CODE.equalsIgnoreCase(MD5Util.string2MD5(SystemProperties.get(BaseConstants.PERSIST_DATA_DEBUG_STATUS, "")));
    }

    public static boolean isUserLogin(){
        if(BaseApplication.userToken != null && !BaseApplication.userToken.isEmpty()){
            return true;
        }
        return false;
    }

    public static void logoutClear(){
        BaseApplication.userToken = null;
    }

    public static void scaleView(View view, float scale){
        ViewCompat.animate(view)
                .setDuration(200)
                .scaleX(scale)
                .scaleY(scale)
                .start();
    }

    public static String getDeviceSn(){
        if(BaseApplication.deviceSn == null || BaseApplication.deviceSn.isEmpty()){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                return Build.getSerial();
            } else {
                return SystemProperties.get("ro.serialno");
            }
        }else{
            return BaseApplication.deviceSn;
        }
    }

    public static String getLogoUrl() {
        return logoUrl;
    }

    public static String setLogoUrl(String url) {
        return logoUrl = url;
    }

}
