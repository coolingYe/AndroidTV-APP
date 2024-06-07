package com.zee.launcher.home.data.protocol.request;

public class MainCategoryReq {
    String categoryType;
    String parentId;

    public MainCategoryReq(String categoryType, String parentId) {
        this.categoryType = categoryType;
        this.parentId = parentId;
    }
}
