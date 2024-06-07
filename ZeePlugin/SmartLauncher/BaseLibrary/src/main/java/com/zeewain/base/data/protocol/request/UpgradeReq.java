package com.zeewain.base.data.protocol.request;

public class UpgradeReq {
    private String originalSoftwareVersion;
    private String softwareCode;

    public UpgradeReq(String originalSoftwareVersion, String softwareCode) {
        this.originalSoftwareVersion = originalSoftwareVersion;
        this.softwareCode = softwareCode;
    }
}
