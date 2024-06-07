package com.zee.launcher.home.gesture.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.util.Log;

import com.google.gson.Gson;
import com.zee.launcher.home.gesture.model.AkSkResp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class CommonUtils {



    public static boolean copyFilesFromAssetsTo(Context context, String[] fileNames, String dirPath) {
        try {
            for (String model : fileNames) {
                copyAssetFileToFiles(context, model, dirPath);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private static void copyAssetFileToFiles(Context context, String filename, String dirPath) throws IOException {
        File of = new File(dirPath + filename);
        if(!of.exists()){
            InputStream is = context.getAssets().open(filename);
            byte[] buffer = new byte[is.available()];
            is.read(buffer);

            of.createNewFile();
            FileOutputStream os = new FileOutputStream(of);
            os.write(buffer);
            os.close();
            is.close();
        }
    }

    public static void saveFile(String fileContent, String filePathName) {
        File file = new File(filePathName);
        if(!file.exists()){
            FileOutputStream fos = null;
            try {
                boolean success = file.createNewFile();
                if(success) {
                    byte[] buffer = fileContent.getBytes(StandardCharsets.UTF_8);
                    fos = new FileOutputStream(file);
                    fos.write(buffer);
                    fos.close();
                }
            }catch (Exception e){
                if(fos != null){
                    try {
                        fos.close();
                    } catch (IOException exception) {
                        exception.printStackTrace();
                    }
                }
            }
        }
    }


    private static final String SETTING_APP_PACKAGE_NAME = "com.zee.setting";
    public static final String Camera = "camera";
    public static final String SetRect = "setRect";
    public static final String ShowOrHide = "showOrHide";
    private static final String SETTING_SP_FILE_NAME = "spUtils";
    public static final String TAG = "CommonUtils";
    private static final String SMART_LAUNCHER_APP_PACKAGE_NAME = "com.zee.launcher";
    public static final String SMART_LAUNCHER_SP_FILE_NAME = "spUtils";
    public static final String AK_SK_INFO = "AkSkInfo";
    public static final String BASE_URL = "BaseUrl";
    public static final String BASE_PATH = "BasePath";



    public static String getCameraId(Context context){
        try {
            Context settingContext = context.createPackageContext(SETTING_APP_PACKAGE_NAME, Context.CONTEXT_IGNORE_SECURITY);
            SharedPreferences sp = settingContext.getSharedPreferences(SETTING_SP_FILE_NAME, Context.MODE_MULTI_PROCESS | Context.MODE_PRIVATE);
            String cameraId = sp.getString(Camera, "");
            return cameraId;

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getSetRect(Context context){
        try {
            Context settingContext = context.createPackageContext(SETTING_APP_PACKAGE_NAME, Context.CONTEXT_IGNORE_SECURITY);
            SharedPreferences sp = settingContext.getSharedPreferences(SETTING_SP_FILE_NAME, Context.MODE_MULTI_PROCESS | Context.MODE_PRIVATE);
            return sp.getString(SetRect, "");

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }


    public static String getShowOrHide(Context context){
        try {
            Context settingContext = context.createPackageContext(SETTING_APP_PACKAGE_NAME, Context.CONTEXT_IGNORE_SECURITY);
            SharedPreferences sp = settingContext.getSharedPreferences(SETTING_SP_FILE_NAME, Context.MODE_MULTI_PROCESS | Context.MODE_PRIVATE);
            return sp.getString(ShowOrHide, "");

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }


    public static AkSkResp getAkSkCode(Context context){
        try {
            Context settingContext = context.createPackageContext(SMART_LAUNCHER_APP_PACKAGE_NAME, Context.CONTEXT_IGNORE_SECURITY);
            SharedPreferences sp = settingContext.getSharedPreferences(SMART_LAUNCHER_SP_FILE_NAME, Context.MODE_MULTI_PROCESS | Context.MODE_PRIVATE);
            String akSkInfoString = sp.getString(AK_SK_INFO, "");
            Log.i(TAG, "akSkInfoString -> " + akSkInfoString);
            if(akSkInfoString != null && !akSkInfoString.isEmpty()) {
                Gson gson = new Gson();
                AkSkResp akSkResp = gson.fromJson(akSkInfoString, AkSkResp.class);
                Log.i(TAG, "akSkResp -> " + akSkResp);
                if(akSkResp != null){
                    return akSkResp;
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "getAkSkCode() err -> " + e);
            e.printStackTrace();
        }
        return null;
    }

    public static String getAuthUrl(Context context){
        try {
            Context settingContext = context.createPackageContext(SMART_LAUNCHER_APP_PACKAGE_NAME, Context.CONTEXT_IGNORE_SECURITY);
            SharedPreferences sp = settingContext.getSharedPreferences(SMART_LAUNCHER_SP_FILE_NAME, Context.MODE_MULTI_PROCESS | Context.MODE_PRIVATE);
            String baseUrl = sp.getString(BASE_URL, "");
            String basePath = sp.getString(BASE_PATH, "");
            Log.i(TAG, "baseUrl=" + baseUrl + ", basePath=" + basePath);
            if(baseUrl != null && !baseUrl.isEmpty()
                    && basePath != null && !basePath.isEmpty()) {
                String useAuthUrl = baseUrl + basePath + "/auth";
                Log.i(TAG, "useAuthUrl -> " + useAuthUrl);
                return useAuthUrl;
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "getAuthUrl() err -> " + e);
            e.printStackTrace();
        }
        return null;
    }

}
