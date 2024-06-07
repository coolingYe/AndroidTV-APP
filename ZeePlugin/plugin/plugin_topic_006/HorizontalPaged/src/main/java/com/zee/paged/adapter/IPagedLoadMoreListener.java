package com.zee.paged.adapter;


import java.util.List;

public interface IPagedLoadMoreListener<T> {
    boolean onLoadMore();
    List<T> getLoadedData();
}
