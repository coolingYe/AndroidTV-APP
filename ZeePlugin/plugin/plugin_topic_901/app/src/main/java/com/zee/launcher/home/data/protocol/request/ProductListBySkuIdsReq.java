package com.zee.launcher.home.data.protocol.request;

import java.util.List;

public class ProductListBySkuIdsReq {
    private List<String> skuIds;

    public ProductListBySkuIdsReq(List<String> skuIds) {
        this.skuIds = skuIds;
    }
}
