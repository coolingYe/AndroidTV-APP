package com.zee.launcher.home.service;

public class CameraErrorException extends Exception {
    public int errorCode;

    public CameraErrorException(String msg, int errorCode) {
        super(msg);
        this.errorCode = errorCode;
    }
}
