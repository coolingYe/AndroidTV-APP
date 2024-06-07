package com.zee.setting.adapter;

import android.annotation.SuppressLint;
import android.net.wifi.WifiConfiguration;
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

public class SavedWifiAdapter extends RecyclerView.Adapter<SavedWifiAdapter.SavedWifiItemHolder>{
    private List<WifiConfiguration> dataList;
    private OnItemClickListener onItemClickListener;

    public SavedWifiAdapter(List<WifiConfiguration> dataList) {
        this.dataList = dataList;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateDataList(List<WifiConfiguration> dataList) {
        this.dataList = dataList;
        notifyDataSetChanged();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @NonNull
    @Override
    public SavedWifiItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_wifi_saved, parent, false);
        return new SavedWifiItemHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SavedWifiItemHolder holder, int position) {
        holder.bind(dataList.get(position));
    }

    @Override
    public int getItemCount() {
        return dataList == null ? 0 : dataList.size();
    }

    class SavedWifiItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private final ConstraintLayout wifiLayout;
        private final TextView wifiName;
        private  ImageView imgLock;
        private final TextView connected;

        public SavedWifiItemHolder(@NonNull View itemView) {
            super(itemView);
            wifiName = itemView.findViewById(R.id.wifi_name);
            imgLock = itemView.findViewById(R.id.img_lock);
            connected = itemView.findViewById(R.id.connected);
            wifiLayout = itemView.findViewById(R.id.wifi_layout);
        }

        public void bind(WifiConfiguration wifiConfiguration) {
            connected.setVisibility(View.INVISIBLE);
            String useSSID = wifiConfiguration.SSID.replace("\"", "");
            wifiName.setText(useSSID);
            wifiLayout.setTag(wifiConfiguration);
            wifiLayout.setOnClickListener(this);

            if (getWifiSecurityLock(wifiConfiguration)) {
                imgLock.setVisibility(View.VISIBLE);
            } else {
                imgLock.setVisibility(View.INVISIBLE);
            }
        }

        @Override
        public void onClick(View v) {
            if (onItemClickListener != null && v.getTag() != null) {
                onItemClickListener.onItemClick(v, (WifiConfiguration)v.getTag());
            }
        }
    }

    public interface OnItemClickListener{
        void onItemClick(View view, WifiConfiguration wifiConfiguration);
    }
    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public static boolean getWifiSecurityLock(WifiConfiguration config) {
        if (config.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.WPA_PSK))
            return true;

        if (config.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.WPA_EAP) || config.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.IEEE8021X))
            return true;

        return config.wepKeys[0] != null;
    }
}
