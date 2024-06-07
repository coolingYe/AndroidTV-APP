package com.zee.setting.data.protocol.request;

public class UpgradeReq {
    private String originalSoftwareVersion;
    private String softwareCode;

    public UpgradeReq(String originalSoftwareVersion, String softwareCode) {
        this.originalSoftwareVersion = originalSoftwareVersion;
        this.softwareCode = softwareCode;
    }

    @Override
    public String toString() {
        return "UpgradeReq{" +
                "originalSoftwareVersion='" + originalSoftwareVersion + '\'' +
                ", softwareCode='" + softwareCode + '\'' +
                '}';
    }
}
