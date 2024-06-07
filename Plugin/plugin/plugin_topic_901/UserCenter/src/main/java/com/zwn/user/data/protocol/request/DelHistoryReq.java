package com.zwn.user.data.protocol.request;

import java.util.List;

public class DelHistoryReq {
    public List<String> skuIds;

    public DelHistoryReq(List<String> skuIds) {
        this.skuIds = skuIds;
    }
}
