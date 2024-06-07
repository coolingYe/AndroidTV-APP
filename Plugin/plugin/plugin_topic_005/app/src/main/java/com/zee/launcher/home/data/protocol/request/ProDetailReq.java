package com.zee.launcher.home.data.protocol.request;

public class ProDetailReq {
    public String skuId;
    public String deviceSn;

    public ProDetailReq(String skuId, String deviceSn) {
        this.skuId = skuId;
        this.deviceSn = deviceSn;
    }
}
