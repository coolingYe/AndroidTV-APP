package com.zee.setting.adapter;

import android.annotation.SuppressLint;
import android.util.Log;
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

public class BondBluetoothAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_FOOTER = 0;
    private static final int VIEW_TYPE_ITEM = 1;
    private List<BlueResult> blueResultList;

    public BondBluetoothAdapter(List<BlueResult> mameList) {
        this.blueResultList = mameList;
    }

    public void resetData(List<BlueResult> list)
    {
        blueResultList = list;
        notifyDataSetChanged();
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_FOOTER) {
            return onCreateFooterViewHolder(parent);
        } else if (viewType == VIEW_TYPE_ITEM) {
            return onCreateItemViewHolder(parent);
        }
        return null;
    }

    @NonNull
    private DeviceNameHolder onCreateItemViewHolder(@NonNull ViewGroup parent) {
        //动态加载布局
        View view = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.list_item_bond_bluetooth, parent, false);
        //创建ViewHolder实例，参数为刚加载进来的子项布局
        DeviceNameHolder viewHolder = new DeviceNameHolder(view);

        return viewHolder;
    }

    public RecyclerView.ViewHolder onCreateFooterViewHolder(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.list_item_clear_bluetooth, parent, false);
        ClearBluetoothItemHolder viewHolder = new ClearBluetoothItemHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, @SuppressLint("RecyclerView") int position) {

        switch (getItemViewType(position)) {
            case VIEW_TYPE_ITEM:
                onBindItemViewHolder(holder, position);
                break;
            case VIEW_TYPE_FOOTER:
                onBindFooterViewHolder(holder, position);
                break;
            default:
                break;
        }

    }

    private void onBindItemViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        DeviceNameHolder nameHolder = (DeviceNameHolder) holder;
        BlueResult result = blueResultList.get(position);
        String deviceName = result.bluetoothDevice.getName();
        nameHolder.deviceName.setText(deviceName);
        if (result.isConnected()==true){
            nameHolder.status.setText("已连接");
        }else {
            nameHolder.status.setText("已配对");
        }
        //type:1是耳机，2是手机
        if (result.type==1){
            nameHolder.imgHeadset.setImageResource(R.drawable.img_headset_selector);
        }else if (result.type==2){
            nameHolder.imgHeadset.setImageResource(R.drawable.img_phone_selector);
        }



        nameHolder.deviceLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (itemClick != null) {
                    itemClick.onItemClick(deviceName, result);
                }
            }
        });
    }

    private void onBindFooterViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        final ClearBluetoothItemHolder holder = (ClearBluetoothItemHolder) viewHolder;
        holder.footType.setText("清除蓝牙配对信息");
        holder.footLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (itemClick != null) {
                    itemClick.onItemClick("清除蓝牙配对信息", null);
                }

            }
        });


    }

    @Override
    public int getItemCount() {
        if (blueResultList == null) {
            return 0;
        }
        return blueResultList.size() + 1;

    }

    @Override
    public int getItemViewType(int position) {
        if (position + 1 == getItemCount()) {//最后一条为FooterView
            return VIEW_TYPE_FOOTER;
        }
        return VIEW_TYPE_ITEM;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public class DeviceNameHolder extends RecyclerView.ViewHolder {

        private ImageView imgHeadset;
        private TextView deviceName;
        private TextView status;
        private ConstraintLayout deviceLayout;

        public DeviceNameHolder(@NonNull View itemView) {
            super(itemView);
            deviceLayout = itemView.findViewById(R.id.bond_layout);
            imgHeadset = itemView.findViewById(R.id.img_headset);
            deviceName = itemView.findViewById(R.id.device_name);
            status = itemView.findViewById(R.id.bond);


        }
    }

    class ClearBluetoothItemHolder extends RecyclerView.ViewHolder {


        private final TextView footType;
        private ConstraintLayout footLayout;

        public ClearBluetoothItemHolder(@NonNull View itemView) {
            super(itemView);
            footLayout = itemView.findViewById(R.id.foot_layout);
            footType = itemView.findViewById(R.id.foot_type);
        }
    }


    private OnItemClick itemClick;   //定义点击事件接口

    public void setItemClick(OnItemClick itemClick) {
        this.itemClick = itemClick;
    }

    //定义一个点击事件的接口
    public interface OnItemClick {
        void onItemClick(String name, BlueResult blueResult);
    }

}
