package com.zee.launcher.home.adapter;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.zee.launcher.home.R;
import com.zee.launcher.home.data.model.ProductListMo;
import com.zee.launcher.home.ui.detail.DetailInterActivity;
import com.zee.launcher.home.widgets.ScanningConstraintLayout;
import com.zeewain.base.config.BaseConstants;
import com.zeewain.base.utils.CommonUtils;
import com.zeewain.base.utils.DisplayUtil;
import com.zeewain.base.utils.GlideApp;

import java.lang.reflect.Method;
import java.util.List;

public class ListAppAdapter extends RecyclerView.Adapter<ListAppAdapter.ListViewHolder> {


    private final List<ProductListMo.Record> dataList;
    private final String packageName;
    private  RecyclerView recyclerView;

    public ListAppAdapter(List<ProductListMo.Record> dataList,String packageName) {
        this.dataList = dataList;
        this.packageName = packageName;
    }
    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        this.recyclerView = recyclerView;
    }

    @NonNull
    @Override
    public ListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_app_list, parent, false);
        return new ListViewHolder(view,packageName);
    }

    @Override
    public void onBindViewHolder(@NonNull ListViewHolder holder, int position) {
        holder.bind(dataList.get(position), position);
    }

    @Override
    public int getItemCount() {
        return dataList == null ? 0 : dataList.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

   static  class ListViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnFocusChangeListener{
        private final ImageView imgGuessLike;
        private final TextView txtGuessLikeTitle;
        public final ScanningConstraintLayout scanningLayout;
        private final MaterialCardView cardGuessLikeRoot;
        private final int strokeWidth;
        private final String  packageName;

        public void bind(ProductListMo.Record record, int position) {
            GlideApp.with(imgGuessLike.getContext())
                    .load(record.getProductImg())
                    .into(imgGuessLike);
            if (record.getProductTitle() != null) {
                txtGuessLikeTitle.setText(record.getProductTitle());
            }
            cardGuessLikeRoot.setTag(record);
            cardGuessLikeRoot.setOnClickListener(this);
//            cardGuessLikeRoot.setOnKeyListener((v, keyCode, event) -> {
//                if (keyCode == KeyEvent.KEYCODE_DPAD_UP && event.getAction() == KeyEvent.ACTION_DOWN) {
//                    if (position <= 9) {
//                        scrollToAmount(recyclerView, 0,-300);
//                    }
//                } else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN && event.getAction() == KeyEvent.ACTION_DOWN){
//                    Log.e("wang", "bind: " + position + "get count " + getItemCount());
//                    if (position >= 5 && position <25 ) {
//                        scrollToAmount(recyclerView, 0,280);
//                    }
//                }
//                return false;
//            });
        }



        public ListViewHolder(@NonNull View view,String pageName) {
            super(view);
            imgGuessLike = view.findViewById(R.id.img_guess_like);
            txtGuessLikeTitle = view.findViewById(R.id.txt_guess_like_title);
            scanningLayout = view.findViewById(R.id.scl_type_classic_root);
            cardGuessLikeRoot = view.findViewById(R.id.card_guess_like_root);
            cardGuessLikeRoot.setOnFocusChangeListener(this);
            strokeWidth = DisplayUtil.dip2px(view.getContext(), 1);
            this.packageName = pageName;
        }

        @Override
        public void onClick(View view) {
            if(view.getTag() != null){
                ProductListMo.Record record = (ProductListMo.Record)view.getTag();
                Intent intent = new Intent(view.getContext(), DetailInterActivity.class);
                intent.putExtra("skuId", record.getSkuId());
                intent.putExtra(BaseConstants.EXTRA_PACKAGE_NAME, packageName);
                view.getContext().startActivity(intent);
            }
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
    }

    private void scrollToAmount(RecyclerView recyclerView, int dx, int dy) {
        try {
            Class<? extends RecyclerView> recClass = recyclerView.getClass();
            Method smoothMethod = recClass.getDeclaredMethod("smoothScrollBy", int.class, int.class, Interpolator.class, int.class);
            smoothMethod.invoke(recyclerView, dx, dy, new AccelerateDecelerateInterpolator(), 400);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
