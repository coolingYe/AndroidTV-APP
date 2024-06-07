package com.zee.setting.camera2;

public class CameraErrorException extends Exception {
    public int errorCode;

    public CameraErrorException(String msg, int errorCode) {
        super(msg);
        this.errorCode = errorCode;
    }
}
