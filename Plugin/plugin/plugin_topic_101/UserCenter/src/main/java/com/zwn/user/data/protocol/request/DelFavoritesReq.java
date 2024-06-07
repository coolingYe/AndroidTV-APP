package com.zwn.user.data.protocol.request;

import java.util.List;

public class DelFavoritesReq {

    public List<String> objIdList;

    public DelFavoritesReq(List<String> objIdList) {
        this.objIdList = objIdList;
    }
}
