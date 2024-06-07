package com.zee.launcher.home.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.zee.launcher.home.R;
import com.zee.launcher.home.data.model.UnitMo;
import com.zee.launcher.home.widgets.banner.BannerAdapter;

import java.util.List;

public class UnitBannerAdapter extends BannerAdapter<List<UnitMo>, UnitBannerAdapter.UnitViewHolder> {

    public UnitBannerAdapter(List<List<UnitMo>> dataList) {
        super(dataList);
    }

    @Override
    public UnitViewHolder onCreateHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_main_unit_layout, parent, false);
        return new UnitViewHolder(view);
    }

    @Override
    public void onBindView(UnitViewHolder holder, List<UnitMo> data, int position, int size) {
        holder.bind(data);
    }

    static class UnitViewHolder extends RecyclerView.ViewHolder {
        private final RecyclerView recyclerViewUnitList;

        public UnitViewHolder(@NonNull View view) {
            super(view);
            recyclerViewUnitList = view.findViewById(R.id.recycler_view_unit_list);
            recyclerViewUnitList.setLayoutManager(new LinearLayoutManager(view.getContext(), LinearLayoutManager.HORIZONTAL, false));
        }

        public void bind(List<UnitMo> data){
            recyclerViewUnitList.setAdapter(new UnitListAdapter(data));
        }
    }
}
