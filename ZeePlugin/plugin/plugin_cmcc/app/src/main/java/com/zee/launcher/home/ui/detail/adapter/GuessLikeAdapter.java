package com.zee.launcher.home.ui.detail.adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.zee.launcher.home.R;
import com.zee.launcher.home.data.model.ProductListMo;
import com.zee.launcher.home.ui.detail.DetailActivity;
import com.zee.launcher.home.widgets.ScanningConstraintLayout;
import com.zeewain.base.utils.CommonUtils;
import com.zeewain.base.utils.GlideApp;

import java.util.List;

public class GuessLikeAdapter extends RecyclerView.Adapter<GuessLikeAdapter.GuessLikeViewHolder> {
    private final List<ProductListMo.Record> dataList;

    public GuessLikeAdapter(List<ProductListMo.Record> dataList) {
        this.dataList = dataList;
    }

    @NonNull
    @Override
    public GuessLikeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_guess_like_layout, parent, false);
        return new GuessLikeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GuessLikeViewHolder holder, int position) {
        holder.bind(dataList.get(position));
    }

    @Override
    public int getItemCount() {
        return dataList == null ? 0 : dataList.size();
    }

    static class GuessLikeViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final ImageView imgGuessLike;
        private final TextView txtGuessLikeTitle;
        public final ScanningConstraintLayout scanningLayout;

        public void bind(ProductListMo.Record record){
            GlideApp.with(imgGuessLike.getContext())
                    .load(record.getProductImg())
                    .into(imgGuessLike);
            if (record.getProductTitle() != null) {
                txtGuessLikeTitle.setText(record.getProductTitle());
            }
            itemView.setTag(record);
        }

        public GuessLikeViewHolder(@NonNull View view) {
            super(view);
            imgGuessLike = view.findViewById(R.id.img_guess_like);
            txtGuessLikeTitle = view.findViewById(R.id.txt_guess_like_title);
            scanningLayout = view.findViewById(R.id.scl_type_classic_root);
            if (CommonUtils.isTopic003Enable()) {
                txtGuessLikeTitle.setVisibility(View.GONE);
            }
            itemView.setBackgroundResource(R.drawable.selector_details_product_frame);
            scanningLayout.setBackgroundResource(R.mipmap.ic_home_detail_product_bg);
            itemView.setOnClickListener(this);
            itemView.setOnFocusChangeListener((v, hasFocused) -> {
                if (hasFocused) {
                    CommonUtils.scaleView(v, 1.1f);
                    scanningLayout.startAnimator();
                } else {
                    v.clearAnimation();
                    CommonUtils.scaleView(v, 1f);
                    scanningLayout.stopAnimator();
                }
            });
        }

        @Override
        public void onClick(View view) {
            if(view.getTag() != null){
                ProductListMo.Record record = (ProductListMo.Record)view.getTag();
                Intent intent = new Intent(view.getContext(), DetailActivity.class);
                intent.putExtra("skuId", record.getSkuId());
                view.getContext().startActivity(intent);
            }
        }
    }
}
