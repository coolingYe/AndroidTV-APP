package com.zee.setting.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.zee.setting.R;
import com.zee.setting.bean.WifiResult;

import java.util.List;

public class WirelessWifiAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private List<WifiResult> datas;
    private Context context;
    private static final int VIEW_TYPE_FOOTER = 0;
    private static final int VIEW_TYPE_ITEM = 1;
    private OnItemClickListener onItemClickListener;

    public WirelessWifiAdapter(List<WifiResult> datas,Context context) {
        this.datas = datas;
        this.context=context;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_FOOTER) {
            return onCreateFooterViewHolder(parent,viewType);
        } else if (viewType == VIEW_TYPE_ITEM) {
            return onCreateItemViewHolder(parent,viewType);
        }

        return null;
    }

    @NonNull
    public RecyclerView.ViewHolder onCreateItemViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.list_item_wireless_wifi, parent, false);
        return new WifiItemHolder(view);


    }

    public RecyclerView.ViewHolder onCreateFooterViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.list_item_add_net, parent, false);

        return new AddNetItemHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        switch (getItemViewType(position)) {
            case VIEW_TYPE_ITEM:
                onBindItemViewHolder( holder, position);
                break;
            case VIEW_TYPE_FOOTER:
                onBindFooterViewHolder(holder, position);
                break;
            default:
                break;
        }

    }

    private void onBindItemViewHolder(@NonNull  RecyclerView.ViewHolder viewHolder, int position) {
        final WifiItemHolder holder= (WifiItemHolder)viewHolder;
        WifiResult wifiResult = datas.get(position);
        holder.wifiName.setText(datas.get(position).name);
        holder.wifiLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(onItemClickListener != null ){
                    onItemClickListener.onItemClick(view, datas.get(position),position);
                }
            }
        });
        if (wifiResult.careType == WifiResult.CARE_TYPE_SELECTED){
            holder.connected.setVisibility(View.VISIBLE);
            holder.connected.setText("已连接");
            holder.imgLock.setImageResource(R.drawable.img_un_lock_selector);
        }else {
            //holder.connected.setVisibility(View.GONE);
            holder.connected.setVisibility(View.VISIBLE);
           // holder.binding.connected.setVisibility(View.VISIBLE);
            holder.imgLock.setImageResource(R.drawable.img_lock_selector);
            if(wifiResult.careType == WifiResult.CARE_TYPE_SAVED_ENABLED){
                holder.connected.setText("已保存");
            }else if(wifiResult.careType == WifiResult.CARE_TYPE_SAVED_DISABLED){
                if (wifiResult.isHavePassword()){
                    holder.connected.setText("密码错误");
                }

               // Toast.makeText(context,"连接"+wifiResult.name+"失败，请检查密码",Toast.LENGTH_SHORT).show();
            }else{
                holder.connected.setText("");

            }
        }

    }

    private void onBindFooterViewHolder(@NonNull  RecyclerView.ViewHolder viewHolder, int position) {

        final AddNetItemHolder holder= (AddNetItemHolder)viewHolder;
        if (position==(getItemCount()-2)){
            holder.footType.setText("添加新网络");
        }else if (position==(getItemCount()-1)){
            holder.footType.setText("网络测速");
        }
        holder.footLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(onItemClickListener != null ){
                    if (position==(getItemCount()-2)){
                        onItemClickListener.onItemClick(view, null,position,"add");
                    }else {
                        onItemClickListener.onItemClick(view, null,position,"speed");
                    }

                }

            }
        });


    }

    @Override
    public int getItemCount() {
        if (datas==null){
            return 0;
        }

        //return datas.size()+1;
        return datas.size()+2;
    }

    @Override
    public int getItemViewType(int position) {
      //  if (position + 1 == getItemCount()) {//最后一条为FooterView
        if (position >=(getItemCount()-2)) {//最后两条条为FooterView
            return VIEW_TYPE_FOOTER;
        }
        return VIEW_TYPE_ITEM;
    }

    class WifiItemHolder extends RecyclerView.ViewHolder {


        private final ConstraintLayout wifiLayout;
        private  ImageView imgWifi;
        private  TextView wifiName;
        private  ImageView imgLock;
        private  TextView connected;

        public WifiItemHolder(@NonNull View itemView) {
            super(itemView);
            imgWifi = itemView.findViewById(R.id.img_wifi);
            wifiName = itemView.findViewById(R.id.wifi_name);
            imgLock = itemView.findViewById(R.id.img_lock);
            connected = itemView.findViewById(R.id.connected);
            wifiLayout = itemView.findViewById(R.id.wifi_layout);
        }
    }
    class AddNetItemHolder extends RecyclerView.ViewHolder {


        private final TextView footType;
        private  ConstraintLayout footLayout;

        public AddNetItemHolder(@NonNull View itemView) {
            super(itemView);
            footLayout = itemView.findViewById(R.id.foot_layout);
            footType = itemView.findViewById(R.id.foot_type);
        }
    }

    public interface OnItemClickListener{
        void onItemClick(View view, WifiResult result,int position,String... footType);
    }
    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }




}
