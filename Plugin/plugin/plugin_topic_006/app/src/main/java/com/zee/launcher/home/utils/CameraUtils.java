package com.zee.launcher.home.utils;

import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;

public class CameraUtils {
    public static int getCameraNum(Context context) {
        CameraManager cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        try {
            String[] cameraIds = cameraManager.getCameraIdList();
            if (cameraIds != null) {
                return cameraIds.length;
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
