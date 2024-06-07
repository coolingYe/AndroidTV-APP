package com.zee.launcher.home.ui.detail.adapter;

import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.zee.launcher.home.R;
import com.zee.launcher.home.data.model.ProductListMo;
import com.zee.launcher.home.ui.detail.DetailActivity;
import com.zee.launcher.home.widgets.ScanningConstraintLayout;
import com.zeewain.base.utils.CommonUtils;
import com.zeewain.base.utils.DisplayUtil;
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

    static class GuessLikeViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnFocusChangeListener{
        private final ImageView imgGuessLike;
        private final TextView txtGuessLikeTitle;
        public final ScanningConstraintLayout scanningLayout;
        private final MaterialCardView cardGuessLikeRoot;
        private final int strokeWidth;

        public void bind(ProductListMo.Record record){
            GlideApp.with(imgGuessLike.getContext())
                    .load(record.getProductImg())
                    .into(imgGuessLike);
            if (record.getProductTitle() != null) {
                txtGuessLikeTitle.setText(record.getProductTitle());
            }
            cardGuessLikeRoot.setTag(record);
            cardGuessLikeRoot.setOnClickListener(this);

        }

        public GuessLikeViewHolder(@NonNull View view) {
            super(view);
            imgGuessLike = view.findViewById(R.id.img_guess_like);
            txtGuessLikeTitle = view.findViewById(R.id.txt_guess_like_title);
            scanningLayout = view.findViewById(R.id.scl_type_classic_root);
            cardGuessLikeRoot = view.findViewById(R.id.card_guess_like_root);
            cardGuessLikeRoot.setOnFocusChangeListener(this);
           // strokeWidth = DisplayUtil.dip2px(view.getContext(), 2);
            strokeWidth = DisplayUtil.dip2px(view.getContext(), 1);
        }

        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if(hasFocus){
                //cardGuessLikeRoot.setStrokeColor(0xFFFA701F);
                cardGuessLikeRoot.setStrokeColor(Color.parseColor("#FFFFFF"));
                cardGuessLikeRoot.setStrokeWidth(strokeWidth);
                CommonUtils.scaleView(v, 1.12f);
            }else{
                cardGuessLikeRoot.setStrokeColor(0x00FFFFFF);
                cardGuessLikeRoot.setStrokeWidth(0);
                v.clearAnimation();
                CommonUtils.scaleView(v, 1f);
            }

            if(hasFocus)
                scanningLayout.startAnimator();
            else
                scanningLayout.stopAnimator();
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
