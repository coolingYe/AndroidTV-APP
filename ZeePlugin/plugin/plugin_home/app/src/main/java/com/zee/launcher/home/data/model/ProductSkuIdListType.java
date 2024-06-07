package com.zee.launcher.home.data.model;


import java.io.Serializable;
import java.util.List;

public class ProductSkuIdListType implements Serializable {

    public String careKey;
    public List<String> skuIdList;

    public ProductSkuIdListType(String careKey, List<String> skuIdList) {
        this.careKey = careKey;
        this.skuIdList = skuIdList;
    }
}
