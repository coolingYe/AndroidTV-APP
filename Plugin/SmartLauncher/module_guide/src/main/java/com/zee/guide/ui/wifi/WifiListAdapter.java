package com.zee.guide.ui.wifi;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.zee.guide.R;
import com.zeewain.base.utils.DisplayUtil;

import java.util.List;

public class WifiListAdapter extends RecyclerView.Adapter<WifiListAdapter.SettingsItemHolder> {
    private final List<WifiResult> dataList;
    private Context context;
    private OnItemClickListener onItemClickListener;

    public WifiListAdapter(List<WifiResult> dataList, Context context) {
        this.dataList = dataList;
        this.context  = context;
    }

    @NonNull
    @Override
    public SettingsItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_wifi, parent, false);
        return new SettingsItemHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SettingsItemHolder holder, int position) {
        holder.bind(dataList.get(position));
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    class SettingsItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private final TextView txtItemWifiTitle;
        private final ImageView imgItemWifiLevel;
        private final TextView txtItemWifiTip;
        private final ImageView imgItemWifiCipher;
        private final LinearLayout llItemWifiRoot;
        private final MaterialCardView cardItemWifiRoot;

        public void bind(WifiResult wifiResult){
            txtItemWifiTitle.setText(wifiResult.name);
            llItemWifiRoot.setTag(wifiResult);
            cardItemWifiRoot.setTag(wifiResult);
            int level = WifiManager.calculateSignalLevel(wifiResult.signalLevel,5);
            if (wifiResult.capabilities.contains("WEP") || wifiResult.capabilities.contains("PSK") ||
                    wifiResult.capabilities.contains("EAP")) {
                if(level <= 1){
                    imgItemWifiLevel.setImageResource(R.mipmap.wifi_lock_level_0);
                }else if(level == 2){
                    imgItemWifiLevel.setImageResource(R.mipmap.wifi_lock_level_1);
                }else if(level == 3){
                    imgItemWifiLevel.setImageResource(R.mipmap.wifi_lock_level_2);
                }else{
                    imgItemWifiLevel.setImageResource(R.mipmap.wifi_lock_level_3);
                }
            } else {
                if(level <= 1){
                    imgItemWifiLevel.setImageResource(R.mipmap.wifi_level_0);
                }else if(level == 2){
                    imgItemWifiLevel.setImageResource(R.mipmap.wifi_level_1);
                }else if(level == 3){
                    imgItemWifiLevel.setImageResource(R.mipmap.wifi_level_2);
                }else{
                    imgItemWifiLevel.setImageResource(R.mipmap.wifi_level_3);
                }
            }

            if(wifiResult.careType == WifiResult.CARE_TYPE_SELECTED){
                txtItemWifiTip.setText("已连接");
                txtItemWifiTip.setVisibility(View.VISIBLE);
                imgItemWifiCipher.setImageResource(R.mipmap.icon_item_next);
            }else {
                imgItemWifiCipher.setImageBitmap(null);
                if(wifiResult.careType == WifiResult.CARE_TYPE_SAVED_ENABLED){
                    txtItemWifiTip.setText("已保存");
                    txtItemWifiTip.setVisibility(View.VISIBLE);
                }else if(wifiResult.careType == WifiResult.CARE_TYPE_SAVED_DISABLED){
                    txtItemWifiTip.setText("请检查密码，然后重试");
                    txtItemWifiTip.setVisibility(View.VISIBLE);
                }else{
                    txtItemWifiTip.setText("");
                    txtItemWifiTip.setVisibility(View.GONE);
                }
            }

            llItemWifiRoot.setOnClickListener(this);
            cardItemWifiRoot.setOnClickListener(this);
        }

        @SuppressLint("ResourceAsColor")
        public SettingsItemHolder(View view) {
            super(view);
            txtItemWifiTitle = view.findViewById(R.id.txt_item_wifi_title);
            imgItemWifiLevel = view.findViewById(R.id.img_item_wifi_level);
            txtItemWifiTip = view.findViewById(R.id.txt_item_wifi_tip);
            imgItemWifiCipher = view.findViewById(R.id.img_item_wifi_cipher);
            llItemWifiRoot = view.findViewById(R.id.ll_item_wifi_root);
            cardItemWifiRoot = view.findViewById(R.id.card_item_wifi_root);
            cardItemWifiRoot.setOnFocusChangeListener((v, hasFocus) -> {
                final int strokeWidth = DisplayUtil.dip2px(v.getContext(), 1);
                if (hasFocus) {
                    cardItemWifiRoot.setStrokeColor(context.getResources().getColor(R.color.selectedStrokeColorBlue));
                    cardItemWifiRoot.setStrokeWidth(strokeWidth);
                } else {
                    cardItemWifiRoot.setStrokeColor(context.getResources().getColor(R.color.unselectedStrokeColor));
                    cardItemWifiRoot.setStrokeWidth(0);
                }
            });
        }

        @Override
        public void onClick(View view) {
            if(onItemClickListener != null && (view.getTag() != null)){
                onItemClickListener.onItemClick(view, (WifiResult) view.getTag());
            }
        }
    }

    public interface OnItemClickListener{
        void onItemClick(View view, WifiResult wifiResult);
    }

}
