package com.zwn.user.data.protocol.request;

import com.zeewain.base.data.protocol.request.PagedBase;

public class FavoritePagedReq extends PagedBase {
    public FavoritePagedReq(int pageNo, int pageSize) {
        this.pageNo = pageNo;
        this.pageSize = pageSize;
    }
}
