package com.zee.launcher.home.gesture.utils;

import android.app.Activity;
import android.app.Application;
import android.app.Service;
import android.util.DisplayMetrics;

public class DensityUtils {
    public final static float WIDTH = 960.f;
    public final static float HEIGHT = 540.f;

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

    public static void autoWidth(Application application, Service service) {
        DisplayMetrics displayMetrics = application.getResources().getDisplayMetrics();
        float targetDensity = displayMetrics.widthPixels / WIDTH;
        int targetDensityDpi = (int) (targetDensity * 160);
        displayMetrics.density = targetDensity;
        displayMetrics.scaledDensity = targetDensity;
        displayMetrics.densityDpi = targetDensityDpi;

        DisplayMetrics activityDisplayMetrics = service.getResources().getDisplayMetrics();
        activityDisplayMetrics.density = targetDensity;
        activityDisplayMetrics.scaledDensity = targetDensity;
        activityDisplayMetrics.densityDpi = targetDensityDpi;
    }


}
