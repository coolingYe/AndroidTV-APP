package com.zee.launcher.home.adapter;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.zee.launcher.home.R;
import com.zee.launcher.home.data.model.ProductListMo;
import com.zee.launcher.home.ui.detail.DetailActivity;
import com.zeewain.base.utils.CommonUtils;
import com.zeewain.base.utils.GlideApp;

import java.util.List;
import java.util.function.Consumer;

public class ProductListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public int TYPE_KEY = 0;
    public static final int TYPE_PERSON_ONE = 0;
    public static final int TYPE_PERSON_TWO = 1;
    public static final int TYPE_PERSON_THREE = 2;

    private List<ProductListMo.Record> dataList;
    private Consumer<ProductListMo.Record> callback;
    private Consumer<Integer> onItemFocusedListens;

    public ProductListAdapter(List<ProductListMo.Record> dataList, int type) {
        this.dataList = dataList;
        this.TYPE_KEY = type;
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
        if (viewType == TYPE_PERSON_ONE) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product_layout, parent, false);
            return new ProductItemViewHolder(view);
        } else if (viewType == TYPE_PERSON_TWO) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_rule_product_layout, parent, false);
            return new CategoryViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_rule_product_layout, parent, false);
            return new ProductItemViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ProductItemViewHolder) {
            ((ProductItemViewHolder) holder).bind(dataList.get(position), position);
        } else {
            ((CategoryViewHolder) holder).bind(dataList.get(position), position);
        }
    }

    @Override
    public int getItemCount() {
        return dataList == null ? 0 : dataList.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (TYPE_KEY == TYPE_PERSON_ONE) {
            return TYPE_PERSON_ONE;
        } else if (TYPE_KEY == TYPE_PERSON_TWO) {
            return TYPE_PERSON_TWO;
        } else return TYPE_PERSON_THREE;
    }

    class ProductItemViewHolder extends RecyclerView.ViewHolder {
        private final ConstraintLayout constraintLayout;
        private final TextView textView;
        private final ImageView imageView;

        public void bind(ProductListMo.Record record, int position) {
            GlideApp.with(imageView.getContext())
                    .load(record.getProductImg())
                    .into(imageView);
            if (record.getProductTitle() != null) {
                textView.setText(record.getProductTitle());
            }
            constraintLayout.setTag(record);
            constraintLayout.setOnFocusChangeListener((v, hasFocus) -> {
                if (hasFocus) {
                    textView.setTextColor(0xFFFEA04F);
                    CommonUtils.scaleView(v, 1.1f);
                    if (TYPE_KEY == TYPE_PERSON_THREE) {
                        if (position == 0) {
                            v.setNextFocusLeftId(v.getId());
                        }
                    }
                    if (onItemFocusedListens != null) {
                        onItemFocusedListens.accept(position);
                    }
                } else {
                    textView.setTextColor(0xFFFFFFFF);
                    v.clearAnimation();
                    CommonUtils.scaleView(v, 1f);
                }
            });
        }

        public ProductItemViewHolder(@NonNull View itemView) {
            super(itemView);
            constraintLayout = itemView.findViewById(R.id.ll_home_rule_product_layout);
            imageView = itemView.findViewById(R.id.iv_home_rule_product);
            textView = itemView.findViewById(R.id.tv_home_rule_product);
            constraintLayout.setOnClickListener(v -> {
                if (v.getTag() != null) {
                    ProductListMo.Record record = (ProductListMo.Record) v.getTag();
                    Intent intent = new Intent(v.getContext(), DetailActivity.class);
                    intent.putExtra("skuId", record.getSkuId());
                    v.getContext().startActivity(intent);
                }
            });
        }
    }

    class CategoryViewHolder extends RecyclerView.ViewHolder {
        private final TextView textView;
        private final ImageView imageView;
        private final ConstraintLayout ruleLayout;

        public void bind(ProductListMo.Record record, int position) {
            itemView.setTag(position);
            textView.setText(record.getProductTitle());
            if (record.getHeat() != 0) {
                imageView.setImageResource(record.getHeat());
            }
            ruleLayout.setOnClickListener(v -> {
                if (onItemFocusedListens != null) {
                    onItemFocusedListens.accept(position);
                }
            });
        }

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.tv_home_rule_product);
            imageView = itemView.findViewById(R.id.iv_home_rule_product);
            ruleLayout = itemView.findViewById(R.id.ll_home_rule_product_layout);
            itemView.setOnFocusChangeListener((v, hasFocus) -> {
                if (hasFocus) {
                    if (v.getTag() != null) {
                        if ((int) v.getTag() == 0) {
                            v.setNextFocusLeftId(v.getId());
                        }
                    }
                    textView.setTextColor(0xFFFEA04F);
                    CommonUtils.scaleView(v, 1.1f);
                } else {
                    v.clearAnimation();
                    textView.setTextColor(0xFFFFFFFF);
                    CommonUtils.scaleView(v, 1f);
                }
            });
        }
    }
}
