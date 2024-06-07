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
import com.zee.setting.bean.BlueResult;

import java.util.List;

public class UnBondBluetoothAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<BlueResult> mameList;

    public UnBondBluetoothAdapter(List<BlueResult> mameList) {
        this.mameList = mameList;
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //动态加载布局
        View view = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.list_item_un_bond_bluetooth, parent, false);
        //创建ViewHolder实例，参数为刚加载进来的子项布局
        DeviceNameHolder viewHolder = new DeviceNameHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        DeviceNameHolder nameHolder = (DeviceNameHolder) holder;
        BlueResult result = mameList.get(position);
        String deviceName=result.bluetoothDevice.getName();
        nameHolder.deviceName.setText(deviceName);
        //type:1是耳机，2是手机
        if (result.type==1){
            nameHolder.imgHeadset.setImageResource(R.drawable.img_headset_selector);
        }else if (result.type==2){
            nameHolder.imgHeadset.setImageResource(R.drawable.img_phone_selector);
        }

        nameHolder.deviceLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (itemClick!=null){
                    itemClick.onItemClick(deviceName,result);
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

        private ImageView imgHeadset;
        private TextView deviceName;
        private ConstraintLayout deviceLayout;

        public DeviceNameHolder(@NonNull View itemView) {
            super(itemView);
            deviceLayout = itemView.findViewById(R.id.un_bond_layout);
            imgHeadset = itemView.findViewById(R.id.img_headset);
            deviceName = itemView.findViewById(R.id.device_name);



        }
    }


    private OnItemClick itemClick;   //定义点击事件接口

    public void setItemClick(OnItemClick itemClick) {
        this.itemClick = itemClick;
    }

    //定义一个点击事件的接口
    public interface OnItemClick {
        void onItemClick(String name,BlueResult blueResult);
    }

}
