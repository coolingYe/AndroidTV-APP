package com.zee.launcher.home.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.load.MultiTransformation;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.zee.launcher.home.R;
import com.zee.launcher.home.data.model.UnitMo;
import com.zeewain.base.utils.DisplayUtil;
import com.zeewain.base.utils.GlideApp;

import java.util.List;

public class UnitListAdapter extends RecyclerView.Adapter<UnitListAdapter.UnitListViewHolder> {

    private final List<UnitMo> dataList;

    public UnitListAdapter(List<UnitMo> dataList) {
        this.dataList = dataList;
    }

    @NonNull
    @Override
    public UnitListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_main_unit_list_layout, parent, false);
        return new UnitListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UnitListViewHolder holder, int position) {
        holder.bind(dataList.get(position));
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    static class UnitListViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivUnitLogo;

        public UnitListViewHolder(@NonNull View view) {
            super(view);
            ivUnitLogo = view.findViewById(R.id.iv_unit_logo);
        }

        public void bind(UnitMo data){
            RequestOptions options = new RequestOptions()
                    .transform(new MultiTransformation(
                            new CenterCrop(),
                            new RoundedCorners(DisplayUtil.dip2px(ivUnitLogo.getContext(), 4))));
            GlideApp.with(ivUnitLogo.getContext())
                    .load(data.logoUrl)
                    .apply(options)
                    .into(ivUnitLogo);
        }
    }
}
