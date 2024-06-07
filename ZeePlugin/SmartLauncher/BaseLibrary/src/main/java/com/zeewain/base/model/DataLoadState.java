package com.zeewain.base.model;

public class DataLoadState<T> {
    public LoadState loadState;
    public T data;

    public DataLoadState(LoadState loadState, T data) {
        this.loadState = loadState;
        this.data = data;
    }

    public DataLoadState(LoadState loadState) {
        this.loadState = loadState;
    }
}
