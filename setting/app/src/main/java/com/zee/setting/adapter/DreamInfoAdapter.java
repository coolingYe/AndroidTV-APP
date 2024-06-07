package com.zee.setting.adapter;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.zee.setting.R;
import com.zee.setting.utils.DreamUtils;

import java.util.List;

public class DreamInfoAdapter extends RecyclerView.Adapter<DreamInfoAdapter.DreamInfoHolder> {

    private List<DreamUtils.DreamInfo> dataList;

    private ComponentName selectedDream;

    public DreamInfoAdapter(List<DreamUtils.DreamInfo> dataList) {
        this.dataList = dataList;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateDataList(List<DreamUtils.DreamInfo> dataList) {
        this.dataList = dataList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DreamInfoHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_dream_action, parent, false);
        return new DreamInfoHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DreamInfoHolder holder, int position) {
        holder.bind(dataList.get(position));
    }


    @Override
    public int getItemCount() {
        return dataList == null ? 0 : dataList.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public class DreamInfoHolder extends RecyclerView.ViewHolder {
        private final TextView tvDreamName;
        private final RadioButton radioBtnDream;

        public DreamInfoHolder(@NonNull View itemView) {
            super(itemView);
            tvDreamName = itemView.findViewById(R.id.dream_action_title);
            radioBtnDream = itemView.findViewById(R.id.dream_action_radio_btn);
        }

        public void bind(DreamUtils.DreamInfo dreamInfo) {

            tvDreamName.setText(dreamInfo.caption);
            if (selectedDream != null) {
                radioBtnDream.setChecked(selectedDream.equals(dreamInfo.componentName));
            }

            radioBtnDream.setClickable(false);

            itemView.setOnClickListener(view -> {
                if (itemClick != null) {
                    itemClick.onItemClick(itemView, dreamInfo);
                }
            });
        }
    }

    private OnItemClick itemClick;

    public void setItemClick(OnItemClick itemClick) {
        this.itemClick = itemClick;
    }


    public interface OnItemClick {
        void onItemClick(View view, DreamUtils.DreamInfo dreamInfo);
    }

    public void setSelectedDream(ComponentName selectedDream) {
        this.selectedDream = selectedDream;
    }

}
