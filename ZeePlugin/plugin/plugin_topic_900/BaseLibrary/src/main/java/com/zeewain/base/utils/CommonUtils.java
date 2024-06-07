package com.zeewain.base.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.view.View;

import androidx.core.view.ViewCompat;

import com.zeewain.base.BaseApplication;
import com.zeewain.base.BuildConfig;
import com.zeewain.base.config.BaseConstants;
import com.zeewain.base.config.SharePrefer;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class CommonUtils {

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

    public static boolean isUserLogin(){
        String userToken = SPUtils.getInstance().getString(SharePrefer.userToken);
        if(userToken != null && !userToken.isEmpty() && BaseApplication.userToken != null && !BaseApplication.userToken.isEmpty()){
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
        if (BuildConfig.FLAVOR == "plugin") {
            if (BaseApplication.deviceSn == null || BaseApplication.deviceSn.isEmpty()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    return Build.getSerial();
                } else {
                    return SystemProperties.get("ro.serialno");
                }
            } else {
                return BaseApplication.deviceSn;
            }
        } else {
            return "rockchip20230331444C1sn";
        }

    }


    public static void saveBitmap(Bitmap b, String path){

        final String jpegName = path + System.currentTimeMillis() +".jpg";
        try {
            FileOutputStream out = new FileOutputStream(jpegName);
            BufferedOutputStream bos = new BufferedOutputStream(out);
            b.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            bos.flush();
            bos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
