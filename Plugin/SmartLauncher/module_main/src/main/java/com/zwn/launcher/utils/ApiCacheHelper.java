package com.zwn.launcher.utils;

import android.content.Context;
import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zee.guide.data.protocol.response.ServicePkgInfoResp;
import com.zeewain.base.config.BaseConstants;
import com.zeewain.base.utils.CareLog;
import com.zeewain.base.utils.DiskCacheHelper;
import com.zeewain.base.utils.DiskCacheManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class ApiCacheHelper {

    public static ServicePkgInfoResp getPluginCacheServicePkgInfoResp(Context context) {
        String pluginServicePkgKey_1 = DiskCacheHelper.toMd5Key(BaseConstants.basePath + BaseConstants.ApiPath.SERVICE_PACKAGE_INFO);
        String pluginServicePkgKey_2 = DiskCacheHelper.toMd5Key(BaseConstants.basePath + BaseConstants.ApiPath.SERVICE_PACKAGE_INFO_2); //topic 101,900, 901, 002 use;

        CareLog.w("pluginServicePkgCacheFile pluginServicePkgKey_1=" + pluginServicePkgKey_1 + ", pluginServicePkgKey_2=" + pluginServicePkgKey_2);
        File pluginCacheDir = new File(context.getExternalCacheDir(), DiskCacheHelper.PLUGIN_API_CACHE_DIR);
        InputStream inputStream = null;
        try {
            File pluginServicePkgCacheFile_1 = new File(pluginCacheDir, pluginServicePkgKey_1 + "." + 0);
            File pluginServicePkgCacheFile_2 = new File(pluginCacheDir, pluginServicePkgKey_2 + "." + 0);
            if (pluginServicePkgCacheFile_1.exists()) {
                inputStream = new FileInputStream(pluginServicePkgCacheFile_1);
            } else if (pluginServicePkgCacheFile_2.exists()) {
                inputStream = new FileInputStream(pluginServicePkgCacheFile_2);
            } else {
                CareLog.w("pluginServicePkgCacheFile not exist!");
                return null;
            }

            Object pluginServicePkgCacheObject = DiskCacheHelper.readObject(inputStream);
            if (pluginServicePkgCacheObject != null) {
                String pluginServicePkgCacheContent = (String)pluginServicePkgCacheObject;
                if (!TextUtils.isEmpty(pluginServicePkgCacheContent)) {
                    JSONObject pluginServicePkgJsonObject = JSON.parseObject(pluginServicePkgCacheContent);
                    JSONObject dataJsonObject = pluginServicePkgJsonObject.getJSONObject("data");
                    if (dataJsonObject != null) {
                        ServicePkgInfoResp servicePkgInfoResp = dataJsonObject.toJavaObject(ServicePkgInfoResp.class);
                        if (servicePkgInfoResp.themeJson == null) {
                            JSONObject layoutJsonObject = dataJsonObject.getJSONObject("layoutJson");
                            if (layoutJsonObject != null) {
                                servicePkgInfoResp.themeJson = layoutJsonObject;
                                return servicePkgInfoResp;
                            }
                        } else {
                            return servicePkgInfoResp;
                        }
                    } else {
                        CareLog.e("pluginServicePkgJsonObject not contain data jsonObject!");
                    }
                } else {
                    CareLog.e("pluginServicePkgCacheContent not contain themeJson!");
                }
            } else {
                CareLog.e("pluginServicePkgCacheObject is null!");
            }
        } catch (Exception ignored) {}
        finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException ignored) {}
            }
        }
        return null;
    }

    public static ServicePkgInfoResp getHostCacheServicePkgInfoResp() {
        try {
            String hostServicePkgCacheContent = DiskCacheManager.getInstance().get(BaseConstants.basePath + BaseConstants.ApiPath.UMS_SERVICE_PACKAGE_INFO);
            if (!TextUtils.isEmpty(hostServicePkgCacheContent) && hostServicePkgCacheContent.contains("themeJson")) {
                JSONObject hostServicePkgJsonObject = JSON.parseObject(hostServicePkgCacheContent);
                JSONObject dataJsonObject = hostServicePkgJsonObject.getJSONObject("data");
                if (dataJsonObject != null) {
                    return dataJsonObject.toJavaObject(ServicePkgInfoResp.class);
                }
            }
        } catch (Exception ignored) {}
        return null;
    }
}
