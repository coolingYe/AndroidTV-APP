package com.zee.launcher.home.widgets.banner;


import android.annotation.SuppressLint;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.zee.launcher.home.R;

import java.util.ArrayList;
import java.util.List;

public abstract class BannerAdapter<T, VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> implements IViewHolder<T, VH> {
    protected List<T> dataList = new ArrayList<>();
    private OnBannerClickListener<T> onBannerClickListener;
    private VH mViewHolder;

    public BannerAdapter(List<T> dataList){
        setDataList(dataList);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setDataList(List<T> dataList) {
        if (dataList == null) {
            dataList = new ArrayList<>();
        }
        this.dataList = dataList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        VH vh = onCreateHolder(parent, viewType);
        vh.itemView.setOnClickListener(v -> {
            if (onBannerClickListener != null) {
                T data = (T) vh.itemView.getTag(R.id.banner_data_key);
                int real = (int) vh.itemView.getTag(R.id.banner_pos_key);
                onBannerClickListener.onBannerClick(v, data, real);
            }
        });
        return vh;
    }

    @Override
    public final void onBindViewHolder(@NonNull VH holder, int position) {
        mViewHolder = holder;
        int real = getRealPosition(position);
        T data = dataList.get(real);
        holder.itemView.setTag(R.id.banner_data_key, data);
        holder.itemView.setTag(R.id.banner_pos_key, real);
        onBindView(holder, dataList.get(real), real, getRealCount());
        if (onBannerClickListener != null) {
            holder.itemView.setOnClickListener(view -> onBannerClickListener.onBannerClick(view, data, real));
        }
    }

    public void onItemEnter(View view, int position){
        if (onBannerClickListener != null) {
            int real = getRealPosition(position);
            T data = dataList.get(real);
            onBannerClickListener.onBannerClick(view, data, real);
        }
    }

    @Override
    public int getItemCount() {
        return getRealCount() > 1 ? getRealCount() + BannerConfig.INCREASE_COUNT : getRealCount();
    }

    public int getRealCount() {
        return dataList.size();
    }

    public int getRealPosition(int position) {
        return getRealPosition(true, position, getRealCount());
    }

    public void setOnBannerClickListener(OnBannerClickListener<T> onBannerClickListener) {
        this.onBannerClickListener = onBannerClickListener;
    }

    public VH getViewHolder() {
        return mViewHolder;
    }

    public static int getRealPosition(boolean isIncrease, int position, int realCount) {
        if (!isIncrease) {
            return position;
        }
        int realPosition;
        if (position == 0) {
            realPosition = realCount - 1;
        } else if (position == realCount + 1) {
            realPosition = 0;
        } else {
            realPosition = position - 1;
        }
        return realPosition;
    }
}
