package com.zee.device.helper.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.zee.device.base.model.DeviceInfo;
import com.zee.device.helper.R;
import com.zee.device.helper.data.model.ConnectionState;
import com.zee.device.helper.data.model.DataConnectionState;

import java.util.List;
import java.util.Vector;

public class MainDeviceInfoAdapter extends RecyclerView.Adapter<MainDeviceInfoAdapter.DeviceInfoViewHolder> {
    private List<DeviceInfo> dataList;
    private OnItemClickListener onItemClickListener;
    public DataConnectionState connectionLoadState;

    public MainDeviceInfoAdapter(List<DeviceInfo> dataList) {
        this.dataList = dataList;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateDataList(List<DeviceInfo> dataList) {
        this.dataList = dataList;
        notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateConnectionLoadState(DataConnectionState connectionLoadState) {
        this.connectionLoadState = connectionLoadState;
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public DeviceInfoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new DeviceInfoViewHolder(getItemView(parent.getContext(),parent));
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull DeviceInfoViewHolder holder, int position) {
        DeviceInfo deviceInfo = dataList.get(position);

        holder.mcvDeviceAction.setTag(deviceInfo);

        holder.tvDeviceSn.setText("SN码:" + deviceInfo.sn);
        holder.tvDeviceName.setText(checkSnCode(deviceInfo.sn));
        holder.tvDeviceSummary.setText("设备IP：" + deviceInfo.ip);
        holder.tvDeviceAction.setText("一键直连");

        if (connectionLoadState != null && connectionLoadState.deviceInfo != null) {
            if (deviceInfo.sn.equals(connectionLoadState.deviceInfo.sn)) {
                if (connectionLoadState.connectionState == ConnectionState.Connecting) {
                    holder.tvDeviceAction.setText("连接中...");
                } else if (connectionLoadState.connectionState == ConnectionState.Connected) {
                    holder.tvDeviceAction.setText("连接成功");
                } else {
                    holder.tvDeviceAction.setText("一键直连");
                }
            }
        }
    }

    private String checkSnCode(String sn) {
        String target = sn.substring(0, 4);
        if (target.equals("1112")) {
            return "紫星PRO终端机";
        }
        return "紫星终端机";
    }

    @Override
    public int getItemCount() {
        return dataList == null ? 0 : dataList.size();
    }

    class DeviceInfoViewHolder extends RecyclerView.ViewHolder {
        TextView tvDeviceSn;
        TextView tvDeviceName;
        TextView tvDeviceSummary;
        MaterialCardView mcvDeviceAction;
        TextView tvDeviceAction;

        TextView tvDeleteAction;
        public DeviceInfoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDeviceSn = itemView.findViewById(R.id.tv_device_sn);
            tvDeviceName = itemView.findViewById(R.id.tv_device_name);
            tvDeviceSummary = itemView.findViewById(R.id.tv_device_summary);
            mcvDeviceAction = itemView.findViewById(R.id.mcv_device_action);
            tvDeviceAction = itemView.findViewById(R.id.tv_device_action);
            tvDeleteAction = itemView.findViewById(R.id.delete);
            mcvDeviceAction.setOnClickListener(v -> {
                if (v.getTag() != null) {
                    DeviceInfo deviceInfo = (DeviceInfo) v.getTag();
                    if (onItemClickListener != null) {
                        onItemClickListener.onItemClick(v, deviceInfo);
                    }
                }
            });
            mcvDeviceAction.setOnLongClickListener(v -> {
                if (v.getTag() != null) {
                    DeviceInfo deviceInfo = (DeviceInfo) v.getTag();
                    if (onItemClickListener != null) {
                        onItemClickListener.onItemLongClick(v, deviceInfo);
                    }
                }
                return true;
            });
        }
    }

    public interface OnItemClickListener {
        void onItemLongClick(View view, DeviceInfo deviceInfo);

        void onItemClick(View view, DeviceInfo deviceInfo);

    }

    private List<View> buildMenuItemList(Context context) {
        List<View> viewList = new Vector<>();
        // 删除选项
        TextView tvRemove = new TextView(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dpToPx(context,80), LinearLayout.LayoutParams.MATCH_PARENT);
        tvRemove.setLayoutParams(params);
        tvRemove.setId(R.id.delete);
        tvRemove.setText("删除");
        tvRemove.setTextSize(17);
        tvRemove.setTextColor(Color.WHITE);
        tvRemove.setGravity(Gravity.CENTER);
        tvRemove.setBackgroundResource(R.drawable.shape_c18_red);
        viewList.add(tvRemove);
        return viewList;
    }


    private View getItemView(Context context,ViewGroup parent) {
        ViewGroup rootView = (ViewGroup) LayoutInflater.from(context).inflate(R.layout.item_list_main_device_root, parent, false);
        List<View> itemList = buildMenuItemList(context);
        if (itemList.size() == 0) {
            return rootView;
        }
        int width = 0;
        // 获取菜单布局的宽度
        View view;
        for (int i = 0; i < itemList.size(); i++) {
            view = itemList.get(i);
            if (view == null) {
                continue;
            }
            width += view.getLayoutParams().width;
        }
        // 构建菜单布局
        LinearLayout menuLayout = new LinearLayout(context);
        menuLayout.setId(R.id.menu);
        menuLayout.setOrientation(LinearLayout.HORIZONTAL);
        // 菜单布局的宽度固定为 width
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                width, LinearLayout.LayoutParams.MATCH_PARENT);
        menuLayout.setLayoutParams(params);
        // 添加菜单布局 item 到菜单布局中
        for (int i = 0; i < itemList.size(); i++) {
            view = itemList.get(i);
            if (view == null) {
                continue;
            }
            menuLayout.addView(view);
        }
        // 将菜单布局添加到根布局中
        rootView.addView(menuLayout);
        return rootView;
    }

    public static int dpToPx(Context context, int dp) {
        float density = context.getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}
