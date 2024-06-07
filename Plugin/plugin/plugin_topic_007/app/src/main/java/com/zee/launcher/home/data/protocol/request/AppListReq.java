package com.zee.launcher.home.data.protocol.request;

public class AppListReq {
    public String softwareCode;
    public String deviceSn;

    public AppListReq(String deviceSn, String softwareCode) {
        this.softwareCode = softwareCode;
        this.deviceSn = deviceSn;
    }
}
