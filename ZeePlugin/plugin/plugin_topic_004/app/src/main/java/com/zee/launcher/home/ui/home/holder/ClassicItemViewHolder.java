package com.zee.launcher.home.ui.home.holder;

import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.zee.launcher.home.R;
import com.zee.launcher.home.data.model.ProductListMo;
import com.zee.launcher.home.ui.detail.DetailActivity;
import com.zee.launcher.home.ui.home.model.ProductListType;
import com.zee.launcher.home.widgets.ScanningConstraintLayout;
import com.zeewain.base.utils.CommonUtils;
import com.zeewain.base.utils.FontUtils;
import com.zeewain.base.utils.GlideApp;

import java.util.ArrayList;
import java.util.List;


public class ClassicItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    public final TextView txtTitle;
    public final ImageView imageView;
    public final ScanningConstraintLayout scanningLayout;
    public final CardView cardView;
    public OnItemFocusChange onItemFocusChange;
    private List<ProductListType> typeList;
    private int tabIndex;

    public void bind(ProductListMo.Record record) {
        GlideApp.with(imageView.getContext())
                .load(record.getProductImg())
                .into(imageView);
        if (record.getProductTitle() != null) {
            txtTitle.setText(record.getProductTitle());
        }
        itemView.setTag(record);
        txtTitle.setTypeface(FontUtils.typefaceMedium);
        itemView.setBackgroundResource(R.drawable.selector_home_product_frame);
        scanningLayout.setBackgroundResource(R.mipmap.ic_home_product_bg);
        txtTitle.setTextColor(0xFFCB1912);
    }

    public ClassicItemViewHolder(@NonNull View view) {
        super(view);
        txtTitle = view.findViewById(R.id.txt_type_classic_title);
        imageView = view.findViewById(R.id.img_type_classic);
        cardView = view.findViewById(R.id.cardView_type_classic);
        scanningLayout = view.findViewById(R.id.scl_type_classic_root);
        itemView.setOnClickListener(this);
        itemView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    CommonUtils.scaleView(v, 1.05f);
                } else {
                    v.clearAnimation();
                    CommonUtils.scaleView(v, 1f);
                }

                if (onItemFocusChange != null) {
                    onItemFocusChange.onFocusChange(v, hasFocus);
                }

                if (hasFocus)
                    scanningLayout.startAnimator();
                else
                    scanningLayout.stopAnimator();
            }
        });
    }

    public void setImageViewHeight(int height) {
        ViewGroup.LayoutParams layoutParams = cardView.getLayoutParams();
        layoutParams.height = height;
    }

    public void setTabIndex(int tabIndex) {
        this.tabIndex = tabIndex;
    }

    public void setTitleSummaryMargin(int titleTopMargin, int summaryTopMargin) {
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) txtTitle.getLayoutParams();
        layoutParams.topMargin = titleTopMargin;

    }

    @Override
    public void onClick(View view) {
        if (view.getTag() != null) {
            ProductListMo.Record record = (ProductListMo.Record) view.getTag();
            Intent intent = new Intent(view.getContext(), DetailActivity.class);
            intent.putExtra("skuId", record.getSkuId());
            intent.putExtra("skuIds", getSkuIds(typeList));
            view.getContext().startActivity(intent);
        }
    }

    public void setTypeList(List<ProductListType> typeList) {
        this.typeList = typeList;
    }

    private ArrayList<String> getSkuIds(List<ProductListType> typeList) {
        ArrayList<String> list = new ArrayList<>();
        typeList.forEach(productListType -> {
            if (productListType.appCardListLayout != null) {
                if (productListType.appCardListLayout.config != null) {
                    list.addAll(productListType.appCardListLayout.config.appSkus);
                }
            }
        });
        return list;
    }

    public void setOnItemFocusChange(OnItemFocusChange onItemFocusChange) {
        this.onItemFocusChange = onItemFocusChange;
    }

    public interface OnItemFocusChange {
        void onFocusChange(View v, boolean hasFocus);
    }
}