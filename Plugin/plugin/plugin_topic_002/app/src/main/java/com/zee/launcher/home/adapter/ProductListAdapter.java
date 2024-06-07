package com.zee.launcher.home.adapter;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.zee.launcher.home.R;
import com.zee.launcher.home.data.model.ProductListMo;
import com.zee.launcher.home.ui.detail.DetailActivity;
import com.zeewain.base.utils.CommonUtils;
import com.zeewain.base.utils.DisplayUtil;
import com.zeewain.base.utils.GlideApp;

import java.util.List;
import java.util.function.Consumer;

public class ProductListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<ProductListMo.Record> dataList;
    private Consumer<ProductListMo.Record> callback;
    private Consumer<Integer> onItemFocusedListens;

    public ProductListAdapter(List<ProductListMo.Record> dataList) {
        this.dataList = dataList;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateList(List<ProductListMo.Record> dataList) {
        this.dataList = dataList;
        notifyDataSetChanged();
    }

    public void setCallback(Consumer<ProductListMo.Record> callback) {
        this.callback = callback;
    }

    public void setOnItemFocusedListens(Consumer<Integer> onItemFocusedListens) {
        this.onItemFocusedListens = onItemFocusedListens;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product_layout, parent, false);
        return new ProductItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ProductItemViewHolder) {
            ((ProductItemViewHolder) holder).bind(dataList.get(position), position);
        }
    }

    @Override
    public int getItemCount() {
        return dataList == null ? 0 : dataList.size();
    }

    class ProductItemViewHolder extends RecyclerView.ViewHolder {
        private final LinearLayout linearLayout;
        private final MaterialCardView materialCardView;
        private final TextView textView;
        private final ImageView imageView;

        public void bind(ProductListMo.Record record, int position) {
            GlideApp.with(imageView.getContext())
                    .load(record.getProductImg())
                    .into(imageView);
            if (record.getProductTitle() != null) {
                textView.setText(record.getProductTitle());
            }
            linearLayout.setTag(record);
            linearLayout.setOnFocusChangeListener((v, hasFocus) -> {
                if (hasFocus) {
                    materialCardView.setStrokeColor(0xFFFFFFFF);
                    materialCardView.setStrokeWidth(DisplayUtil.dip2px(itemView.getContext(), 1.5f));
                    CommonUtils.scaleView(v, 1.13f);
                    if (callback != null) {
                        callback.accept(record);
                    }
                    if (onItemFocusedListens != null) {
                        onItemFocusedListens.accept(position);
                    }
                } else {
                    materialCardView.setStrokeColor(0x00FFFFFF);
                    materialCardView.setStrokeWidth(0);
                    v.clearAnimation();
                    CommonUtils.scaleView(v, 1f);
                }
            });
        }

        public ProductItemViewHolder(@NonNull View itemView) {
            super(itemView);
            linearLayout = itemView.findViewById(R.id.ll_main_product_root);
            materialCardView = itemView.findViewById(R.id.card_main_product);
            imageView = itemView.findViewById(R.id.iv_main_item_product);
            textView = itemView.findViewById(R.id.tv_main_product_title);
            linearLayout.setOnClickListener(v -> {
                if (v.getTag() != null) {
                    ProductListMo.Record record = (ProductListMo.Record) v.getTag();
                    Intent intent = new Intent(v.getContext(), DetailActivity.class);
                    intent.putExtra("skuId", record.getSkuId());
                    v.getContext().startActivity(intent);
                }
            });
        }
    }
}
