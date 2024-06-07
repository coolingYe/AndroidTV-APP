package com.zee.launcher.home.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zee.launcher.home.data.layout.GlobalLayout;


public class GlobalLayoutHelper {

    public static GlobalLayout analysisGlobalLayout(JSONObject globalLayoutJsonObject) {

        return JSON.toJavaObject(globalLayoutJsonObject, GlobalLayout.class);
    }
}
