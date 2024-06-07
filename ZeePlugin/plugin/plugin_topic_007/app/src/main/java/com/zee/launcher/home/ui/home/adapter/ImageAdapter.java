package com.zee.launcher.home.ui.home.adapter;

import android.content.Intent;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.draggable.library.extension.ImageViewerHelper;
import com.youth.banner.adapter.BannerAdapter;
import com.zee.launcher.home.data.protocol.response.ShowBannerBean;
import com.zee.launcher.home.ui.detail.DetailActivity;
import com.zeewain.base.utils.GlideApp;

import java.util.List;

public class ImageAdapter extends BannerAdapter<ShowBannerBean, ImageAdapter.BannerViewHolder> {
    public ImageAdapter(List<ShowBannerBean> mDatas) {
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
    public void onBindView(BannerViewHolder holder, ShowBannerBean data, int position, int size) {
        GlideApp.with(holder.itemView)
                .load(data.url)
                .into(holder.imageView);

    }

    class BannerViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public BannerViewHolder(ImageView view) {
            super(view);
            this.imageView = view;
        }
    }
}

