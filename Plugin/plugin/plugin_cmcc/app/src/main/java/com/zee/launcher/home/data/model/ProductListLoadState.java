package com.zee.launcher.home.data.model;

import com.zeewain.base.model.LoadState;

public class ProductListLoadState {
    public String categoryId;
    public LoadState loadState;

    public ProductListLoadState(String categoryId, LoadState loadState) {
        this.categoryId = categoryId;
        this.loadState = loadState;
    }
}
