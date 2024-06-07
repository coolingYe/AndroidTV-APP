package com.zee.device.base.utils;

import android.util.Log;

import com.zee.device.base.BuildConfig;


public class Logger {
    private static final String TAG = "ZeeDev";
    public static boolean debug = true;

    public static void w(String tag, String msg){
        if(debug) {
            if (tag.isEmpty()) {
                Log.w(TAG, msg);
            } else {
                Log.w(TAG, "[" + tag + "] " + msg);
            }
        }
    }

    public static void d(String tag, String msg){
        if(BuildConfig.DEBUG) {
            if (tag.isEmpty()) {
                Log.d(TAG, msg);
            } else {
                Log.d(TAG, "[" + tag + "] " + msg);
            }
        }
    }

    public static void e(String tag, String msg){
        if(debug) {
            if (tag.isEmpty()) {
                Log.e(TAG, msg);
            } else {
                Log.e(TAG, "[" + tag + "] " + msg);
            }
        }
    }

    public static void w(String msg){
        if(debug) {
            Log.w(TAG, msg);
        }
    }

    public static void d(String msg){
        if(BuildConfig.DEBUG) {
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
                Log.i(TAG, "[" + tag + "] " + msg);
            }
        }
    }
}
