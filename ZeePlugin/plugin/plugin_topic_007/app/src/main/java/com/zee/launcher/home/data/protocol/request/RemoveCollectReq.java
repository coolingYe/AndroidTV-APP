package com.zee.launcher.home.data.protocol.request;

import java.util.List;

public class RemoveCollectReq {
    private List<String> objIdList;

    public RemoveCollectReq(List<String> objIdList) {
        this.objIdList = objIdList;
    }
}
