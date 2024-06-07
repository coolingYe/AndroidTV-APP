package com.zee.launcher.home.gesture;

import android.os.Build;
import android.util.Log;

public class HardwareInfo {
    public static boolean careHardware = false;

    public static void init(){
        if("amlogic".equals(Build.HARDWARE) || "rk30board".equals(Build.HARDWARE)){
            Log.e("wang", "init: " + Build.HARDWARE);
            careHardware = true;
        }
    }
}
