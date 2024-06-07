package com.zee.setting.bean;


public class CameraBean {
    public static final int TYPE_USB_CAMERA = 0;
    public static final int TYPE_REMOTE_CAMERA = 1;

    public String cameraName;
    public String cameraId;
    public int type;

    public CameraBean(String cameraName, String cameraId, int type) {
        this.cameraName = cameraName;
        this.cameraId = cameraId;
        this.type = type;
    }

    public String getCameraName() {
        return cameraName;
    }

    public void setCameraName(String cameraName) {
        this.cameraName = cameraName;
    }

    public String getCameraId() {
        return cameraId;
    }

    public void setCameraId(String cameraId) {
        this.cameraId = cameraId;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "CameraBean{" +
                "cameraName='" + cameraName + '\'' +
                ", cameraId='" + cameraId + '\'' +
                ", type=" + type +
                '}';
    }
}
