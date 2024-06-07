package com.zee.launcher.home.data.protocol.request;

import java.io.Serializable;

public class CoursewareStartReq implements Serializable {

    public String skuId;
    public String skuName;
    public String skuUrl;

    public CoursewareStartReq(String skuId, String skuName, String skuUrl) {
        this.skuId = skuId;
        this.skuName = skuName;
        this.skuUrl = skuUrl;
    }
}
