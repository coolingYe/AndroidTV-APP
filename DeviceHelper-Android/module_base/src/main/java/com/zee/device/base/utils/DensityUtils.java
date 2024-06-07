package com.zee.device.base.utils;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.graphics.Point;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

public class DensityUtils {
    public final static float WIDTH = 375.f;
    public final static float HEIGHT = 667.f;

    public static void autoWidth(Application application, Activity activity) {
        DisplayMetrics displayMetrics = application.getResources().getDisplayMetrics();
        float targetDensity = displayMetrics.widthPixels / WIDTH;
        int targetDensityDpi = (int) (targetDensity * 160);

        displayMetrics.density = targetDensity;
        displayMetrics.scaledDensity = targetDensity;
        displayMetrics.densityDpi = targetDensityDpi;

        DisplayMetrics activityDisplayMetrics = activity.getResources().getDisplayMetrics();
        activityDisplayMetrics.density = targetDensity;
        activityDisplayMetrics.scaledDensity = targetDensity;
        activityDisplayMetrics.densityDpi = targetDensityDpi;
    }

    public static void autoHeight(Application application, Activity activity) {
        DisplayMetrics displayMetrics = application.getResources().getDisplayMetrics();
        float targetDensity = displayMetrics.heightPixels / HEIGHT;
        int targetDensityDpi = (int) (targetDensity * 160);

        displayMetrics.density = targetDensity;
        displayMetrics.scaledDensity = targetDensity;
        displayMetrics.densityDpi = targetDensityDpi;

        DisplayMetrics activityDisplayMetrics = activity.getResources().getDisplayMetrics();
        activityDisplayMetrics.density = targetDensity;
        activityDisplayMetrics.scaledDensity = targetDensity;
        activityDisplayMetrics.densityDpi = targetDensityDpi;
    }

    public static Point getScreenSize(Application application) {
        Point screenSize = new Point();

        WindowManager windowManager = (WindowManager) application.getSystemService(Context.WINDOW_SERVICE);
        if (windowManager != null) {
            Display display = windowManager.getDefaultDisplay();
            display.getRealSize(screenSize);
        }

        return screenSize;
    }
}
