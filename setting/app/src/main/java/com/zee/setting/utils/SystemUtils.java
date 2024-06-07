package com.zee.setting.utils;

import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.PowerManager;
import android.view.Display;
import android.view.Window;
import android.view.WindowManager;

public class SystemUtils {

    /**
     * 重启手机
     */
    public static void rebootSystem(Context context) {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        pm.reboot(null);
    }

    //PowerManager.FULL_WAKE_LOCK |
    public static void wakeUpSystem(Context context) {
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "Zee::WakeLock");
        wakeLock.acquire();
        wakeLock.release();
        wakeLock.setReferenceCounted(false);
    }

    public static void sleepSystem(Context context) {
        DevicePolicyManager devicePolicyManager = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        devicePolicyManager.lockNow();
    }

    /**
     * 恢复出厂设置，需要系统权限，以及系统签名 android:sharedUserId="android.uid.system"
     */
    public static void resetSystem(Context context) {
        Intent intent = new Intent("android.intent.action.FACTORY_RESET");
        //8.0
        // intent = new Intent("android.intent.action.MASTER_CLEAR");
        //9.0
        intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
        intent.setPackage("android");

        //以上区分不同系统
        intent.putExtra("android.intent.extra.REASON", "FactoryMode");
        //是否擦除SdCard
        intent.putExtra("android.intent.extra.WIPE_EXTERNAL_STORAGE", true);
        intent.putExtra("android.intent.extra.EXTRA_WIPE_ESIMS", true);
        context.sendBroadcast(intent);
    }

    /**
     * 获取屏幕分辨率
     */

    public static void select4kDisplayMode(Context context, Window window) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return;
        }

        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        Display.Mode[] modes = display.getSupportedModes();
        if (modes == null) {
            return;
        }

        Display.Mode selected = null;
        long max = 0;
        for (Display.Mode mode : modes) {
          /*  Log.d(TAG, "available display mode: Mode %d: %dx%d/%.1ffps", mode.getModeId(),
                    mode.getPhysicalWidth(), mode.getPhysicalHeight(),
                    mode.getRefreshRate());*/
         /*   Log.i("ssshhh", "getModeId=" + mode.getModeId() + "getPhysicalWidth=" + mode.getPhysicalWidth()
                    + "getPhysicalHeight=" + mode.getPhysicalHeight() + "getRefreshRate=" + mode.getRefreshRate());*/
            long val = mode.getPhysicalWidth() * mode.getPhysicalHeight();
            if (val > max) {
                max = val;
                selected = mode;
            }
        }
        if (selected != null) {
            WindowManager.LayoutParams params = window.getAttributes();
            params.preferredDisplayModeId = selected.getModeId();
            window.setAttributes(params);


          /*  Log.d("ssshh", "selected display mode: Mode %d: %dx%d/%.1ffps", selected.getModeId(),
                    selected.getPhysicalWidth(), selected.getPhysicalHeight(),
                    selected.getRefreshRate());*/
        }
    }



/*    @RequiresApi(api = Build.VERSION_CODES.M)
    private void setMode(Activity activity, Display.Mode mode) {
        Window window = activity.getWindow();
        WindowManager.LayoutParams params = window.getAttributes();
        params.preferredDisplayModeId = mode.getModeId();
        window.setAttributes(params); //通过该函数通知wms layout变化。
    }*/



/*    //读取系统支持的Display.mode：
    @RequiresApi(api = Build.VERSION_CODES.R)
    private Display.Mode[] getDisplayModes() {
        Display primaryDisplay = getDisplay();
        Display.Mode[] modes = primaryDisplay.getSupportedModes();

        return modes;//返回该display支持的所有mode的数组， activity可从中选择自己需要的mode.
    }*/

}
