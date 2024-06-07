package com.zee.device.home.data.protocol.request;

public class UserActionRecordReq {
    public String deviceModel;
    public String eventCode;
    public String moudleName;
    public long userId = 0;

    public UserActionRecordReq(String deviceModel, String eventCode, String moudleName) {
        this.deviceModel = deviceModel;
        this.eventCode = eventCode;
        this.moudleName = moudleName;
    }
}
