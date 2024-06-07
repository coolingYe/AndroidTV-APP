package com.zeewain.base.utils;

import com.google.gson.Gson;

/**
 * Created by JuAn_Zhangsongzhou on 2017/5/5.
 * <p>
 * 单例 Gson
 */

public class ZeeWainGson {

    public static Gson getInstance() {
        return Holder.instance;
    }

    private static class Holder {
        private static final Gson instance = new Gson();
    }


}
