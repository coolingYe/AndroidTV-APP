package com.zwn.user.data.model;

public class HistoryItem {
    public String url;
    public String title;
    public String skuId;
    public String recordId;

    public HistoryItem(String url, String title, String skuId, String recordId) {
        this.url = url;
        this.title = title;
        this.skuId = skuId;
        this.recordId = recordId;
    }
}
