package com.zee.launcher.home.ui.cache.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.zeewain.base.utils.CommonUtils;
import com.zeewain.base.utils.GlideApp;
import com.zwn.lib_download.model.DownloadInfo;
import com.zwn.user.R;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class CacheItemAdapter extends RecyclerView.Adapter<CacheItemAdapter.UserProductViewHolder> {
    private List<DownloadInfo> dataList = new ArrayList<>();
    private OnItemClickListener mOnItemClickListener;
    private OnRemoveItemListener mOnRemoveItemListener;
    private boolean mDelMode = false;
    private Consumer<Integer> mOnItemFocusedListener;

    @NonNull
    @Override
    public UserProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_product, parent, false);
        return new UserProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserProductViewHolder holder, int position) {
        holder.bind(dataList.get(position), position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return dataList == null ? 0 : dataList.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateDataList(List<DownloadInfo> dataList) {
        this.dataList = dataList;
        notifyDataSetChanged();
    }

    public void delItem(int position) {
        dataList.remove(position);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setDelMode(boolean delMode) {
        mDelMode = delMode;
        notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void clearData() {
        dataList.clear();
        notifyDataSetChanged();
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position, DownloadInfo downloadInfo);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    public interface OnRemoveItemListener {
        void onRemove(View view, int position, DownloadInfo downloadInfo);
    }

    public void setOnRemoveItemListener(OnRemoveItemListener onRemoveItemListener) {
        mOnRemoveItemListener = onRemoveItemListener;
    }

    public void setOnItemFocusedListener(Consumer<Integer> mOnItemFocusedListener) {
        this.mOnItemFocusedListener = mOnItemFocusedListener;
    }

    public Object getItem(int position) {
        return dataList.get(position);
    }

    public class UserProductViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivItemUserProductImg;
        private final TextView tvItemUserProductTitle;
        private final TextView tvItemUserProductTip;
        private final ImageView ivItemUserProductDel;

        public void bind(DownloadInfo downloadInfo, int position) {
            GlideApp.with(ivItemUserProductImg.getContext())
                    .load(downloadInfo.fileImgUrl)
                    .into(ivItemUserProductImg);
            tvItemUserProductTitle.setText(downloadInfo.fileName);
            if (mDelMode) {
                ivItemUserProductDel.setVisibility(View.VISIBLE);
                tvItemUserProductTip.setVisibility(View.INVISIBLE);
            } else {
                ivItemUserProductDel.setVisibility(View.INVISIBLE);
                if (downloadInfo.status != DownloadInfo.STATUS_SUCCESS) {
                    tvItemUserProductTip.setVisibility(View.VISIBLE);
                    if (downloadInfo.status == DownloadInfo.STATUS_STOPPED) {
                        tvItemUserProductTip.setText("已暂停");
                    } else if (downloadInfo.status == DownloadInfo.STATUS_PENDING) {
                        tvItemUserProductTip.setText("等待中");
                    } else {
                        if (downloadInfo.fileSize > 0) {
                            int progress = (int) ((downloadInfo.loadedSize * 1.0f / downloadInfo.fileSize) * 100);
                            tvItemUserProductTip.setText(progress + "%");
                        } else {
                            tvItemUserProductTip.setText("0%");
                        }
                    }
                } else {
                    tvItemUserProductTip.setVisibility(View.INVISIBLE);
                }
            }

            if (itemView.getNextFocusLeftId() == -1) {
                itemView.setNextFocusLeftId(R.id.recycler_view_user_center_category);
            }
            if (mOnItemClickListener != null) {
                itemView.setOnClickListener(v -> {
                    if (ivItemUserProductDel.getVisibility() == View.VISIBLE) {
                        if (mOnRemoveItemListener != null) {
                            mOnRemoveItemListener.onRemove(v, position, downloadInfo);
                        }
                    }
                    mOnItemClickListener.onItemClick(v, position, downloadInfo);
                });
            }
            ivItemUserProductDel.setOnClickListener(v -> {
                if (mOnRemoveItemListener != null) {
                    mOnRemoveItemListener.onRemove(v, position, downloadInfo);
                }
            });

            itemView.setOnFocusChangeListener((v, hasFocus) -> {
                if (hasFocus) {
                    if (mOnItemFocusedListener != null) {
                        mOnItemFocusedListener.accept(position);
                    }
                    CommonUtils.scaleView(v, 1.1f);
                } else {
                    v.clearAnimation();
                    CommonUtils.scaleView(v, 1f);
                }
            });
        }

        public UserProductViewHolder(View view) {
            super(view);
            ivItemUserProductImg = view.findViewById(R.id.iv_item_user_product_img);
            tvItemUserProductTitle = view.findViewById(R.id.tv_item_user_product_title);
            tvItemUserProductTip = view.findViewById(R.id.tv_item_user_product_tip);
            ivItemUserProductDel = view.findViewById(R.id.iv_item_user_product_del);
            itemView.setBackgroundResource(R.drawable.selector_home_product_frame);
            ConstraintLayout clUserProduct = itemView.findViewById(R.id.cl_user_center_product);
            clUserProduct.setBackgroundResource(R.drawable.ic_user_center_product);
            tvItemUserProductTitle.setTextColor(view.getContext().getColor(R.color.user_center_product_title_color));
        }
    }
}