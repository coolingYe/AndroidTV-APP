package com.zee.launcher.home.data.model;

import com.zeewain.base.model.LoadState;

public class PagedModuleListLoadState extends ProductListLoadState{
    public String type;
    public int pageNum;


    public PagedModuleListLoadState(String categoryId, String type, int pageNum, LoadState loadState) {
        super(categoryId, loadState);
        this.type = type;
        this.pageNum = pageNum;
    }
}
