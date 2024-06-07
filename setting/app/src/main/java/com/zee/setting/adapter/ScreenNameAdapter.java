package com.zee.setting.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;


import com.zee.setting.R;

import java.util.List;

public class ScreenNameAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<String> screenNameList;

    private String selectName;

    public ScreenNameAdapter(List<String> mameList) {
        this.screenNameList = mameList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //动态加载布局

        View view = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.list_screen_name_item, parent, false);
        //创建ViewHolder实例，参数为刚加载进来的子项布局
        ScreenNameHolder viewHolder = new ScreenNameHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        ScreenNameHolder screenHolder = (ScreenNameHolder) holder;
        String deviceName= screenNameList.get(position);
        screenHolder.screenName.setText(deviceName);

        screenHolder.screenLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (itemClick!=null){
                    itemClick.onItemClick(screenNameList.get(position));
                }
            }
        });
        if (screenHolder.screenLayout.isFocused()){
            if (deviceName.equals(selectName)){
                screenHolder.imgScreenCheck.setImageResource(R.mipmap.img_checked_true_white);
            }else {
                screenHolder.imgScreenCheck.setImageResource(R.mipmap.img_checked_false_white);
            }
        }else {
            if (deviceName.equals(selectName)){
                screenHolder.imgScreenCheck.setImageResource(R.mipmap.img_checked_true_grey);
            }else {
                screenHolder.imgScreenCheck.setImageResource(R.mipmap.img_checked_false_grey);
            }
        }
        screenHolder.screenLayout.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus){
                    if (deviceName.equals(selectName)){
                        screenHolder.imgScreenCheck.setImageResource(R.mipmap.img_checked_true_grey);
                    }else {
                        screenHolder.imgScreenCheck.setImageResource(R.mipmap.img_checked_false_grey);
                    }
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        if (screenNameList == null) {
            return 0;
        }
        return screenNameList.size();

    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public class ScreenNameHolder extends RecyclerView.ViewHolder {

        private ImageView imgScreenCheck;
        private TextView screenName;
        private ConstraintLayout screenLayout;

        public ScreenNameHolder(@NonNull View itemView) {
            super(itemView);
            screenLayout = itemView.findViewById(R.id.screen_layout);
            screenName = itemView.findViewById(R.id.screen_name);
            imgScreenCheck = itemView.findViewById(R.id.img_check);

        }
    }


    private OnItemClick itemClick;   //定义点击事件接口

    public void setItemClick(OnItemClick itemClick) {
        this.itemClick = itemClick;
    }

    //定义一个点击事件的接口
    public interface OnItemClick {
        void onItemClick(String name);
    }

    public void setSelect(String name){
        selectName=name;
    }
}
