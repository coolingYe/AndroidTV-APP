package com.zee.launcher.home.ui.home.adapter;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.zee.launcher.home.R;
import com.zee.launcher.home.data.model.ProductListMo;
import com.zee.launcher.home.ui.detail.DetailActivity;
import com.zee.launcher.home.widgets.MyVideoView;
import com.zee.launcher.home.widgets.banner.BannerAdapter;
import com.zee.launcher.home.widgets.banner.OnBannerClickListener;
import com.zeewain.base.utils.CommonUtils;
import com.zeewain.base.utils.FontUtils;
import com.zeewain.base.utils.GlideApp;

import java.util.List;

public class ClassicBannerAdapter extends BannerAdapter<ProductListMo.Record, ClassicBannerAdapter.BannerViewHolder>
        implements OnBannerClickListener<ProductListMo.Record> {

    private boolean showTitleSummary;

    public ClassicBannerAdapter(List<ProductListMo.Record> dataList) {
        super(dataList);
        setOnBannerClickListener(this);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateDataList(List<ProductListMo.Record> dataList, int typeKey) {
        this.dataList = dataList;
        notifyDataSetChanged();
    }

    @Override
    public BannerViewHolder onCreateHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_classic_banner_layout, parent, false);
        return new BannerViewHolder(view);
    }

    @Override
    public void onBindView(BannerViewHolder holder, ProductListMo.Record data, int position, int size) {
        holder.bind(data, showTitleSummary);
    }

    @Override
    public long getItemId(int position) {
        return dataList.get(getRealPosition(position)).hashCode();
    }

    @Override
    public void onBannerClick(View view, ProductListMo.Record record, int position) {
        if (view != null) {
            if (record.getSkuId() != null) {
                Intent intent = new Intent(view.getContext(), DetailActivity.class);
                intent.putExtra("skuId", record.getSkuId());
                view.getContext().startActivity(intent);
            }
        }
    }

    public void setShowTitleSummary(boolean showTitleSummary) {
        this.showTitleSummary = showTitleSummary;
    }

    public static class BannerViewHolder extends RecyclerView.ViewHolder {
        public ConstraintLayout clMask;
        public MyVideoView videoView;
        public TextView txtClassicBannerTitle;
        public TextView txtClassicBannerSummary;
        public ImageView imgClassicBanner;
        public View bottomTextBg;

        public void bind(ProductListMo.Record record, boolean showTitleSummary) {
            switch (record.getKind()) {
                case "img":
                case "app":
                    String imgUrl = "";
                    if (record.getKind().equals("img")) {
                        imgUrl = record.getResourceUrl();
                    } else if (record.getKind().equals("app")) {
                        imgUrl = record.getProductImg();
                        if (record.getExtendInfo() != null) {
                            if (record.getExtendInfo().getBannerImages() != null && record.getExtendInfo().getBannerImages().size() > 0) {
                                imgUrl = record.getExtendInfo().getBannerImages().get(0);
                            }
                        }
                        if (showTitleSummary) {
                            txtClassicBannerTitle.setText(record.getProductTitle());
                            txtClassicBannerSummary.setText(record.getSimplerIntroduce());
                        }
                    }
                    if (imgUrl.length() > 0) {
                        GlideApp.with(imgClassicBanner.getContext())
                                .load(imgUrl)
                                .into(imgClassicBanner);
                    }
                    break;
            }
        }

        public BannerViewHolder(View view) {
            super(view);
            clMask = view.findViewById(R.id.cl_banner_mask);
            videoView = view.findViewById(R.id.banner_video_view);
            txtClassicBannerTitle = view.findViewById(R.id.txt_classic_banner_title);
            txtClassicBannerSummary = view.findViewById(R.id.txt_classic_banner_summary);
            imgClassicBanner = view.findViewById(R.id.img_classic_banner);
            bottomTextBg = view.findViewById(R.id.banner_bottom_bg);
            if (CommonUtils.isTopic003Enable()) {
                txtClassicBannerTitle.setTypeface(FontUtils.typefaceMedium);
                txtClassicBannerSummary.setTypeface(FontUtils.typefaceRegular);
            }
        }
    }
}
