package com.zeewain.base.utils;

import android.util.Log;

import com.zeewain.base.BuildConfig;

public class CareLog {
    private static final String TAG = "ZeeLog";
    public static boolean debug = BuildConfig.DEBUG;

    public static void w(String tag, String msg){
        if(debug) {
            if (tag.isEmpty()) {
                Log.w(TAG, msg);
            } else {
                Log.w(TAG, "[" + tag + "]" + msg);
            }
        }
    }

    public static void d(String tag, String msg){
        if(debug) {
            if (tag.isEmpty()) {
                Log.d(TAG, msg);
            } else {
                Log.d(TAG, "[" + tag + "]" + msg);
            }
        }
    }

    public static void e(String tag, String msg){
        if(debug) {
            if (tag.isEmpty()) {
                Log.e(TAG, msg);
            } else {
                Log.e(TAG, "[" + tag + "]" + msg);
            }
        }
    }

    public static void w(String msg){
        if(debug) {
            Log.w(TAG, msg);
        }
    }

    public static void d(String msg){
        if(debug) {
            Log.d(TAG, msg);
        }
    }

    public static void e(String msg){
        if(debug) {
            Log.e(TAG, msg);
        }
    }

    public static void i(String tag, String msg){
        if(debug) {
            if (tag.isEmpty()) {
                Log.i(TAG, msg);
            } else {
                Log.i(TAG, "[" + tag + "]" + msg);
            }
        }
    }
}
