package com.zwn.launcher.data.protocol.request;

public class SnSoftwareCodeReq {

    private String deviceSn;

    private String softwareCode;

    public SnSoftwareCodeReq(String deviceSn, String softwareCode) {
        this.deviceSn = deviceSn;
        this.softwareCode = softwareCode;
    }
}

