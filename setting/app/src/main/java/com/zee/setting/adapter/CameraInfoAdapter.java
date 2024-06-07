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
import com.zee.setting.bean.CameraBean;

import java.util.List;

public class CameraInfoAdapter extends RecyclerView.Adapter<CameraInfoAdapter.CameraInfoHolder> {

    private List<CameraBean> dataList;

    private String selectedCameraId;

    public CameraInfoAdapter(List<CameraBean> dataList) {
        this.dataList = dataList;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateDataList(List<CameraBean> dataList) {
        this.dataList = dataList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CameraInfoHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_camera_info, parent, false);
        return new CameraInfoHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CameraInfoHolder holder, int position) {
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

    public class CameraInfoHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnFocusChangeListener {

        private final ImageView ivCameraInfoCheck;
        private final ImageView ivCameraIcon;
        private final TextView tvCameraInfoTitle;
        private final TextView tvCameraInfoDesc;
        private final ConstraintLayout clCameraInfoLayout;

        public CameraInfoHolder(@NonNull View itemView) {
            super(itemView);
            ivCameraIcon = itemView.findViewById(R.id.iv_camera_icon);
            clCameraInfoLayout = itemView.findViewById(R.id.cl_camera_info_layout);
            tvCameraInfoTitle = itemView.findViewById(R.id.tv_camera_info_title);
            tvCameraInfoDesc = itemView.findViewById(R.id.tv_camera_info_desc);
            ivCameraInfoCheck = itemView.findViewById(R.id.iv_camera_info_check);

            clCameraInfoLayout.setOnClickListener(this);
            clCameraInfoLayout.setOnFocusChangeListener(this);
        }

        public void bind(CameraBean cameraBean) {
            clCameraInfoLayout.setTag(cameraBean);
            tvCameraInfoTitle.setText(cameraBean.cameraName);
            if (clCameraInfoLayout.isFocused()) {
                if (cameraBean.cameraId.equals(selectedCameraId)) {
                    ivCameraInfoCheck.setImageResource(R.mipmap.ic_camera_selected);
                    tvCameraInfoDesc.setText("连接中");
                } else {
                    ivCameraInfoCheck.setImageResource(R.mipmap.img_checked_false_white);
                    tvCameraInfoDesc.setText("");
                }
            } else {
                if (cameraBean.cameraId.equals(selectedCameraId)) {
                    ivCameraInfoCheck.setImageResource(R.mipmap.ic_camera_selected);
                    tvCameraInfoDesc.setText("连接中");
                } else {
                    ivCameraInfoCheck.setImageResource(R.mipmap.img_checked_false_grey);
                    tvCameraInfoDesc.setText("");
                }
            }
        }

        @Override
        public void onClick(View v) {
            if (itemClick != null && v.getTag() != null) {
                itemClick.onItemClick(v, (CameraBean) v.getTag());
            }
        }

        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (!hasFocus && v.getTag() != null) {
                CameraBean cameraBean = (CameraBean) v.getTag();
                if (cameraBean.cameraId.equals(selectedCameraId)) {
                    ivCameraInfoCheck.setImageResource(R.mipmap.ic_camera_selected);
                } else {
                    ivCameraInfoCheck.setImageResource(R.mipmap.img_checked_false_grey);
                }
            }

            if (hasFocus && onItemFocusedListener != null) {
                onItemFocusedListener.onFocused(v);
            }
        }
    }

    private OnItemFocusedListener onItemFocusedListener;

    public void setOnItemFocusedListener(OnItemFocusedListener onItemFocusedListener) {
        this.onItemFocusedListener = onItemFocusedListener;
    }

    public interface OnItemFocusedListener{
        void onFocused(View v);
    }


    private OnItemClick itemClick;

    public void setItemClick(OnItemClick itemClick) {
        this.itemClick = itemClick;
    }


    public interface OnItemClick {
        void onItemClick(View view, CameraBean cameraBean);
    }

    public void setSelectedCameraId(String selectedCameraId) {
        this.selectedCameraId = selectedCameraId;
    }

}
