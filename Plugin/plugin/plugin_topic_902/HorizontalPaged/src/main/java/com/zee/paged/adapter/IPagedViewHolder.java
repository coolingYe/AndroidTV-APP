package com.zee.paged.adapter;

import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

public interface IPagedViewHolder {
    RecyclerView.ViewHolder onCreateHolder(ViewGroup parent, int viewType);
    void onBindView(RecyclerView.ViewHolder holder,  int position);
}
