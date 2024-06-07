package com.zee.device.home.ui.device.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.zee.device.base.config.SharePrefer;
import com.zee.device.base.db.DatabaseController;
import com.zee.device.base.model.DeviceInfo;
import com.zee.device.base.utils.SPUtils;
import com.zee.device.home.R;

import java.util.List;

public class DeviceInfoAdapter extends RecyclerView.Adapter<DeviceInfoAdapter.DeviceInfoViewHolder> {
    private List<DeviceInfo> dataList;
    private OnItemClickListener onItemClickListener;
    private String selectedDeviceSn;
    private boolean isDelMode = false;

    public DeviceInfoAdapter(List<DeviceInfo> dataList, String selectedDeviceSn) {
        this.dataList = dataList;
        this.selectedDeviceSn = selectedDeviceSn;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateDataList(List<DeviceInfo> dataList){
        this.dataList = dataList;
        notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setDelMode(boolean delMode) {
        isDelMode = delMode;
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public DeviceInfoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_device_layout, parent, false);
        return new DeviceInfoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DeviceInfoViewHolder holder, int position) {
        DeviceInfo deviceInfo = dataList.get(position);
        holder.ivDeviceAction.setTag(deviceInfo);

        holder.tvDeviceName.setText(deviceInfo.name);
        holder.tvDeviceSummary.setText(deviceInfo.sn);

        if(!isDelMode) {
            if (deviceInfo.sn.equals(selectedDeviceSn)) {
                holder.ivDeviceAction.setImageResource(R.mipmap.icon_tick_selected);
            } else {
                holder.ivDeviceAction.setImageResource(R.mipmap.icon_tick_unselect);
            }
        }else{
            holder.ivDeviceAction.setImageResource(R.mipmap.icon_red_round_delete);
        }
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    class DeviceInfoViewHolder extends RecyclerView.ViewHolder{
        TextView tvDeviceName;
        TextView tvDeviceSummary;
        ImageView ivDeviceAction;

        public DeviceInfoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDeviceName = itemView.findViewById(R.id.tv_device_name);
            tvDeviceSummary = itemView.findViewById(R.id.tv_device_summary);
            ivDeviceAction = itemView.findViewById(R.id.iv_device_action);

            ivDeviceAction.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(v.getTag() != null){
                        DeviceInfo deviceInfo = (DeviceInfo)v.getTag();
                        if(isDelMode){
                            DatabaseController.instance.deleteDeviceInfo(deviceInfo.sn);
                            notifyDataSetChanged();
                        }else{
                            if(!deviceInfo.sn.equals(selectedDeviceSn)){
                                selectedDeviceSn = deviceInfo.sn;
                                SPUtils.getInstance().put(SharePrefer.SELECTED_DEVICE_SN, deviceInfo.sn);
                                notifyDataSetChanged();
                            }
                        }
                    }
                }
            });
        }
    }

    public interface OnItemClickListener{
        void onItemLongClick(DeviceInfo deviceInfo);
        void onLanClick(DeviceInfo deviceInfo);
        void onWiFiDirectClick(DeviceInfo deviceInfo);
    }
}
