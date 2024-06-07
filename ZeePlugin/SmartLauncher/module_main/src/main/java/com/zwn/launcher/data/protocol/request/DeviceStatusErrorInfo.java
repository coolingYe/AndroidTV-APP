package com.zwn.launcher.data.protocol.request;

public class DeviceStatusErrorInfo {

    public int code;
    public String msg;

    public DeviceStatusErrorInfo(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
