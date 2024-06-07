package com.zee.launcher.home.data.protocol.request;

public class ProductRecommendReq {
    public String skuId;
    public int topN;

    public ProductRecommendReq(String skuId, int topN) {
        this.skuId = skuId;
        this.topN = topN;
    }
}
