package com.zee.paged.model;

import com.zeewain.base.model.LoadState;

import java.util.List;

public class                                                                                                                                                    PagedListLoadState<T>{
    public String pagedListId;
    public LoadState loadState;
    public int pageNum;
    public List<T> dataList;


    public PagedListLoadState(String pagedListId, LoadState loadState, int pageNum) {
        this.pagedListId = pagedListId;
        this.loadState = loadState;
        this.pageNum = pageNum;
    }

    public PagedListLoadState(String pagedListId, LoadState loadState, int pageNum, List<T> dataList) {
        this.pagedListId = pagedListId;
        this.loadState = loadState;
        this.pageNum = pageNum;
        this.dataList = dataList;
    }


}
