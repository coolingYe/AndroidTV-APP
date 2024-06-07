package com.zee.launcher.home.data.model;

import com.zeewain.base.model.LoadState;

public class ProductRecordListLoadState {
    public String careKey;
    public LoadState loadState;

    public ProductRecordListLoadState(String careKey, LoadState loadState) {
        this.careKey = careKey;
        this.loadState = loadState;
    }
}
