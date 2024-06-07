package com.zee.setting.bean;

public class GuideInfo {
    private String appUrl;
    private String connectQRInfo;
    private DeviceInfo deviceInfo = new DeviceInfo();

    public String getAppUrl() {
        return appUrl;
    }

    public void setAppUrl(String appUrl) {
        this.appUrl = appUrl;
    }

    public DeviceInfo getDeviceInfo() {
        return deviceInfo;
    }

    public void setDeviceInfo(DeviceInfo deviceInfo) {
        this.deviceInfo = deviceInfo;
    }

    public String getConnectQRInfo() {
        return connectQRInfo;
    }

    public void setConnectQRInfo(String connectQRInfo) {
        this.connectQRInfo = connectQRInfo;
    }

    public static class DeviceInfo {
        private String networkName = "";
        private String deviceSN;
        private String ipAddress;

        public String getNetworkName() {
            return networkName;
        }

        public void setNetworkName(String networkName) {
            this.networkName = networkName;
        }

        public String getDeviceSN() {
            return deviceSN;
        }

        public void setDeviceSN(String deviceSN) {
            this.deviceSN = deviceSN;
        }

        public String getIpAddress() {
            return ipAddress;
        }

        public void setIpAddress(String ipAddress) {
            this.ipAddress = ipAddress;
        }
    }
}
