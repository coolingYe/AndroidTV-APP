package com.zee.launcher.home.data.protocol.request;

import java.util.List;

public class ProductListBySkuIdsReq {
    public List<String> skuIds;
    public String deviceSn;

    public ProductListBySkuIdsReq(List<String> skuIds, String deviceSn) {
        this.skuIds = skuIds;
        this.deviceSn = deviceSn;
    }
}
