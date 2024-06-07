package com.zee.launcher.home.ui.home.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.zee.launcher.home.R;
import com.zee.launcher.home.config.LayoutConf;
import com.zee.launcher.home.data.model.ProductListMo;
import com.zeewain.base.utils.DisplayUtil;


import java.util.List;

public class ClassicBannerIndexAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_NORMAL = 0;
    private static final int TYPE_SELECTED = 1;

    private List<ProductListMo.Record> dataList;
    private final int bannerType;
    private int selectIndex = 0;
    private OnBannerIndexSelectListener onBannerIndexSelectListener;

    public ClassicBannerIndexAdapter(List<ProductListMo.Record> dataList, int bannerType){
        this.dataList = dataList;
        this.bannerType = bannerType;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == TYPE_SELECTED){
            if(bannerType == LayoutConf.BannerType.RIGHT_SIDE) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_classic_banner_index_select_layout, parent, false);
                return new BannerIndexSelectViewHolder(view);
            }
        }else{
            if(bannerType == LayoutConf.BannerType.RIGHT_SIDE) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_classic_banner_index_layout, parent, false);
                return new BannerIndexViewHolder(view);
            }
        }

        ImageView imageView = new ImageView(parent.getContext());
        /*ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.MATCH_PARENT);*/
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
                DisplayUtil.dip2px(parent.getContext(), 10),
                DisplayUtil.dip2px(parent.getContext(), 10));
        imageView.setLayoutParams(params);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        return new CommonBannerIndexViewHolder(imageView);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(holder instanceof BannerIndexSelectViewHolder){
            ((BannerIndexSelectViewHolder)holder).bind(dataList.get(position), position);
        }else if(holder instanceof BannerIndexViewHolder){
            ((BannerIndexViewHolder)holder).bind(dataList.get(position), position);
        }else{
            ((CommonBannerIndexViewHolder)holder).bind(dataList.get(position), position);
        }
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    @Override
    public int getItemViewType(int position) {
        if(selectIndex == position){
            return TYPE_SELECTED;
        }
        return TYPE_NORMAL;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateDataList(List<ProductListMo.Record> dataList){
        this.dataList = dataList;
        notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setSelectIndex(int selectIndex){
        this.selectIndex = selectIndex;
        notifyDataSetChanged();
    }

    public int getSelectIndex() {
        return selectIndex;
    }

    public void setOnBannerIndexSelectListener(OnBannerIndexSelectListener onBannerIndexSelectListener) {
        this.onBannerIndexSelectListener = onBannerIndexSelectListener;
    }

    class BannerIndexViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private final View itemView;
        private final TextView txtTitle;

        public void bind(ProductListMo.Record record, int position){
            itemView.setTag(position);
            itemView.setOnClickListener(this);

            txtTitle.setText(record.getProductTitle());
        }

        public BannerIndexViewHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView = itemView;
            this.itemView.setOnClickListener(this);

            txtTitle = itemView.findViewById(R.id.txt_banner_index_title);
        }

        @Override
        public void onClick(View v) {
            if(onBannerIndexSelectListener != null && v.getTag() != null){
                Integer selectedPosition = (Integer)v.getTag();
                onBannerIndexSelectListener.onBannerIndexSelected(selectedPosition);
            }
        }
    }

    class BannerIndexSelectViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private final View itemView;
        private final TextView txtTitle;
        private final TextView txtSummary;

        public void bind(ProductListMo.Record record, int position){
            itemView.setTag(position);
            itemView.setOnClickListener(this);

            txtTitle.setText(record.getProductTitle());
            if(record.getSimplerIntroduce() != null)
                txtSummary.setText(record.getSimplerIntroduce());
        }

        public BannerIndexSelectViewHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView = itemView;
            this.itemView.setOnClickListener(this);

            txtTitle = itemView.findViewById(R.id.txt_banner_index_title);
            txtSummary  = itemView.findViewById(R.id.txt_banner_index_summary);
        }

        @Override
        public void onClick(View v) {
            if(onBannerIndexSelectListener != null && v.getTag() != null){
                Integer selectedPosition = (Integer)v.getTag();
                onBannerIndexSelectListener.onBannerIndexSelected(selectedPosition);
            }
        }
    }

    class CommonBannerIndexViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        ImageView imageView;
        public void bind(ProductListMo.Record record, int position){
            imageView.setTag(position);
            imageView.setOnClickListener(this);

            if (getItemCount() <= 1 ) return;

            if(selectIndex == position){
                imageView.setImageResource(R.mipmap.ic_index_selected);
            }else{
                imageView.setImageResource(R.mipmap.ic_index_normal);
            }
        }

        public CommonBannerIndexViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = (ImageView) itemView;
        }

        @Override
        public void onClick(View v) {
            if(onBannerIndexSelectListener != null && v.getTag() != null){
                Integer selectedPosition = (Integer)v.getTag();
                onBannerIndexSelectListener.onBannerIndexSelected(selectedPosition);
            }
        }
    }

    public interface OnBannerIndexSelectListener{
        void onBannerIndexSelected(int position);
    }
}
