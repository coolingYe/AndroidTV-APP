package com.zee.device.home.ui.home.adapter;

import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.youth.banner.adapter.BannerAdapter;

import java.util.List;

public class ImageAdapter extends BannerAdapter<String,ImageAdapter.BannerViewHolder> {
    public ImageAdapter(List<String> mDatas) {
        //设置数据，也可以调用banner提供的方法,或者自己在adapter中实现
        super(mDatas);
    }

    //创建ViewHolder，可以用viewType这个字段来区分不同的ViewHolder
    @Override
    public BannerViewHolder onCreateHolder(ViewGroup parent, int viewType) {
        ImageView imageView = new ImageView(parent.getContext());
        //注意，必须设置为match_parent，这个是viewpager2强制要求的
        imageView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        return new BannerViewHolder(imageView);
    }

    @Override
    public void onBindView(BannerViewHolder holder, String data, int position, int size) {
        int imgId=0;
        if (position%2==0){
            imgId=com.zee.resource.R.mipmap.icon_digital_human;
        }else {
            imgId=com.zee.resource.R.mipmap.icon_popularize_law;
        }
        Glide.with(holder.itemView)
//                .load(data)
                .load(imgId)
                .apply(RequestOptions.bitmapTransform(new RoundedCorners(15)))
                .into(holder.imageView);
    }

    class BannerViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public BannerViewHolder( ImageView view) {
            super(view);
            this.imageView = view;
        }
    }
}

