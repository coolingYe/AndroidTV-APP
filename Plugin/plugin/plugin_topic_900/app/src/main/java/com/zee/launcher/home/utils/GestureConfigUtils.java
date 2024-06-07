package com.zee.launcher.home.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import com.google.gson.Gson;
import com.zee.launcher.home.data.protocol.response.AkSkResp;


import android.util.Log;

public class GestureConfigUtils {
    public static final String TAG = "CommonUtils";
    private static final String SMART_LAUNCHER_APP_PACKAGE_NAME = "com.zee.launcher";
    public static final String SMART_LAUNCHER_SP_FILE_NAME = "spUtils";
    public static final String AK_SK_INFO = "AkSkInfo";
    public static final String BASE_URL = "BaseUrl";
    public static final String BASE_PATH = "BasePath";
    private static final String SETTING_APP_PACKAGE_NAME = "com.zee.setting";
    private static final String SETTING_SP_FILE_NAME = "spUtils";
    public static final String Camera = "camera";

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
