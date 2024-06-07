package com.zee.launcher.home.gesture.utils;

public class CameraBean {
    public String device;
    public String cameraId;

    public CameraBean(String device, String cameraId) {
        this.device = device;
        this.cameraId = cameraId;
    }

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    public String getCameraId() {
        return cameraId;
    }

    public void setCameraId(String cameraId) {
        this.cameraId = cameraId;
    }
}
