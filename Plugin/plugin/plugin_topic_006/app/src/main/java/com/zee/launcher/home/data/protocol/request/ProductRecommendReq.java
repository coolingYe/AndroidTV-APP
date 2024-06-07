package com.zee.launcher.home.data.protocol.request;

public class ProductRecommendReq {
    public String skuId;
    public String deviceSn;
    public int topN;

    public ProductRecommendReq(String skuId, String deviceSn, int topN) {
        this.skuId = skuId;
        this.deviceSn = deviceSn;
        this.topN = topN;
    }
}

