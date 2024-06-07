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

public class DeviceNameAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<String> mameList;

    private String selectName;

    public DeviceNameAdapter(List<String> mameList) {
        this.mameList = mameList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //动态加载布局
        View view = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.list_device_name_item, parent, false);
        //创建ViewHolder实例，参数为刚加载进来的子项布局
        DeviceNameHolder viewHolder = new DeviceNameHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        DeviceNameHolder nameHolder = (DeviceNameHolder) holder;
        String deviceName=mameList.get(position);
        nameHolder.deviceName.setText(deviceName);

        nameHolder.deviceNameLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (itemClick!=null){
                    itemClick.onItemClick(mameList.get(position));
                }
            }
        });
        if (nameHolder.deviceNameLayout.isFocused()){
            if (deviceName.equals(selectName)){
                nameHolder.imgDeviceCheck.setImageResource(R.mipmap.img_checked_true_white);
            }else {
                nameHolder.imgDeviceCheck.setImageResource(R.mipmap.img_checked_false_white);
            }
        }else {
            if (deviceName.equals(selectName)){
                nameHolder.imgDeviceCheck.setImageResource(R.mipmap.img_checked_true_grey);
            }else {
                nameHolder.imgDeviceCheck.setImageResource(R.mipmap.img_checked_false_grey);
            }
        }
        nameHolder.deviceNameLayout.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus){
                    if (deviceName.equals(selectName)){
                        nameHolder.imgDeviceCheck.setImageResource(R.mipmap.img_checked_true_grey);
                    }else {
                        nameHolder.imgDeviceCheck.setImageResource(R.mipmap.img_checked_false_grey);
                    }
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        if (mameList == null) {
            return 0;
        }
        return mameList.size();

    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public class DeviceNameHolder extends RecyclerView.ViewHolder {

        private ImageView imgDeviceCheck;
        private TextView deviceName;
        private ConstraintLayout deviceNameLayout;

        public DeviceNameHolder(@NonNull View itemView) {
            super(itemView);
            deviceNameLayout = itemView.findViewById(R.id.device_name_layout);
            deviceName = itemView.findViewById(R.id.device_name);
            imgDeviceCheck = itemView.findViewById(R.id.img_device_check);

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

    public String getSelectName() {
        return selectName;
    }
}
