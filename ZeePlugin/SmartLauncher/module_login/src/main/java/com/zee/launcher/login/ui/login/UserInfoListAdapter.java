package com.zee.launcher.login.ui.login;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.zee.launcher.login.R;
import com.zee.launcher.login.data.protocol.response.UserInfoResp;
import com.zeewain.base.utils.DisplayUtil;

import java.util.List;

public class UserInfoListAdapter extends RecyclerView.Adapter<UserInfoListAdapter.UserInfoItemHolder> {

    private List<UserInfoResp> dataList;
    private OnItemClickListener onItemClickListener;

    public UserInfoListAdapter(List<UserInfoResp> dataList) {
        this.dataList = dataList;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateDataList(List<UserInfoResp> dataList) {
        this.dataList = dataList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UserInfoItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_user_info, parent, false);
        return new UserInfoItemHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserInfoItemHolder holder, int position) {
        holder.bind(dataList.get(position));
    }

    @Override
    public int getItemCount() {
        return dataList == null ? 0 : dataList.size();
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    class UserInfoItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final TextView txtItemUserCode;
        private final ImageView imgItemUserPic;
        private final TextView txtItemUserName;
        private final LinearLayout llItemUserInfoRoot;
        private final MaterialCardView cardItemUserInfoRoot;

        public UserInfoItemHolder(@NonNull View view) {
            super(view);
            txtItemUserCode = view.findViewById(R.id.txt_item_user_code);
            imgItemUserPic = view.findViewById(R.id.img_item_user_pic);
            txtItemUserName = view.findViewById(R.id.txt_item_user_name);
            llItemUserInfoRoot = view.findViewById(R.id.ll_item_user_info_root);
            cardItemUserInfoRoot = view.findViewById(R.id.card_item_user_info_root);

            cardItemUserInfoRoot.setOnFocusChangeListener((v, hasFocus) -> {
                final int strokeWidth = DisplayUtil.dip2px(v.getContext(), 1);
                if (hasFocus) {
                    llItemUserInfoRoot.setBackgroundResource(R.mipmap.img_list_item_focus);
                } else {
                    llItemUserInfoRoot.setBackground(null);
                }
            });
        }

        public void bind(UserInfoResp userInfoResp) {
            txtItemUserCode.setText(userInfoResp.userCode);
            llItemUserInfoRoot.setTag(userInfoResp);
            cardItemUserInfoRoot.setTag(userInfoResp);
            llItemUserInfoRoot.setOnClickListener(this);
            cardItemUserInfoRoot.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (onItemClickListener != null && (view.getTag() != null)) {
                onItemClickListener.onItemClick(view, (UserInfoResp) view.getTag());
            }
        }
    }

    public interface OnItemClickListener {
        void onItemClick(View view, UserInfoResp userInfoResp);
    }
}
