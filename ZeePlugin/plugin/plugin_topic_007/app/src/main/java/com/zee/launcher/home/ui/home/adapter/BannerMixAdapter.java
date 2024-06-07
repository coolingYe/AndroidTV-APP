package com.zee.launcher.home.ui.home.adapter;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.GranularRoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.youth.banner.adapter.BannerAdapter;
import com.zee.launcher.home.R;
import com.zee.launcher.home.data.protocol.response.ShowBannerBean;
import com.zee.launcher.home.widgets.MyVideoView;
import com.zeewain.base.utils.DisplayUtil;

import java.util.ArrayList;
import java.util.List;


public class BannerMixAdapter extends BannerAdapter<ShowBannerBean, RecyclerView.ViewHolder> {

    protected Context mContext;
    protected LayoutInflater mInflater;
    protected List<ShowBannerBean> mDataList = new ArrayList<>();
    private MyVideoView myvideo = null;
    public static final int VEDIO = 1;
    public static final int IMAGE = 2;
    private ImageView videoPreview;
    private Handler handler = new Handler();

    public BannerMixAdapter(Context context, List<ShowBannerBean> dataList) {
        super(dataList);
        mContext = context;
        mDataList = dataList;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }


    //通过isVedio获取返回不同的布局
    @Override
    public int getItemViewType(int position) {
        int position2 = position % mDataList.size();
        if (mDataList.get(position2).getKind().equals("video")) {
            return VEDIO;
        } else {
            return IMAGE;
        }

    }


    @Override
    public RecyclerView.ViewHolder onCreateHolder(ViewGroup parent, int viewType) {
        View view;
        RecyclerView.ViewHolder holder;
        LayoutInflater from = LayoutInflater.from(mContext);
        if (viewType == VEDIO) {
            view = from.inflate(R.layout.item_videopay, parent, false);
            holder = new VideoHolder(view);
        } else {
            view = from.inflate(R.layout.item_banner_image, parent, false);

            holder = new ImageHolder(view);
        }
        return holder;
    }

    @Override
    public void onBindView(RecyclerView.ViewHolder holder, ShowBannerBean data, int position, int size) {
        if (holder instanceof VideoHolder) {
            setVideo(holder, data, position, size);
        } else {
            setImage(holder, data, position, size);
        }

    }


    private void setImage(RecyclerView.ViewHolder holder, ShowBannerBean data, int position, int size) {
        ImageView image = ((ImageHolder) holder).image;
        Glide.with(mContext)
                .load(data.getUrl())
                .into(image);


    }

    private void setVideo(RecyclerView.ViewHolder holder, ShowBannerBean data, int position, int size) {
        //视频缩略图

        videoPreview = ((VideoHolder) holder).videoPreview;
        Glide.with(mContext)
                .load(data.getUrl())
                .into(videoPreview);
        myvideo = ((VideoHolder) holder).video;

        myvideo.setVideoURI(Uri.parse(data.getUrl()));
      /*      myvideo.setZOrderMediaOverlay(true);
            myvideo.setZOrderOnTop(true);*/

        myvideo.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                myvideo.start();

            }
        });

        myvideo.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                if (data.play.equals("start")){
                    myvideo.start();
                    myvideo.setZOrderMediaOverlay(true);
                    videoPreview.setVisibility(View.GONE);
                }else if (data.play.equals("stop")){
                    myvideo.pause();
                    myvideo.setZOrderMediaOverlay(false);
                    videoPreview.setVisibility(View.VISIBLE);
                }

            }
        });




    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }



/*    @Override
    public int getItemCount() {
        return mDataList.size();
    }*/


    class VideoHolder extends RecyclerView.ViewHolder {
        private MyVideoView video;
        private ImageView videoPreview;
        private  FrameLayout videoRoot;


        public VideoHolder(View itemView) {
            super(itemView);
            video = itemView.findViewById(R.id.banner_vp);
            videoPreview = itemView.findViewById(R.id.video_preview);
            videoRoot = itemView.findViewById(R.id.video_root);
        }
    }

    class ImageHolder extends RecyclerView.ViewHolder {
        private ImageView image;

        public ImageHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.banner_iv);
        }
    }


}

