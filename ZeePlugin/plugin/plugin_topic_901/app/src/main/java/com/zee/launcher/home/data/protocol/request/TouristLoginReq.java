package com.zee.launcher.home.data.protocol.request;

public class TouristLoginReq {


    public String type;
    public String appCode;
    public String deviceSn;

    public TouristLoginReq(String deviceSn) {
        this.type = "4";
        this.appCode = "mall_ums";
        this.deviceSn = deviceSn;
    }
}
