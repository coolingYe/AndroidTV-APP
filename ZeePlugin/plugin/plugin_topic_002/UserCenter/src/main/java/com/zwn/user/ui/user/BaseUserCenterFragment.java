package com.zwn.user.ui.user;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.zeewain.base.utils.CommonUtils;
import com.zeewain.base.utils.DisplayUtil;
import com.zeewain.base.widgets.CenterGridLayoutManager;
import com.zeewain.base.widgets.LoadingView;
import com.zeewain.base.widgets.NetworkErrView;
import com.zwn.user.R;
import com.zwn.user.adapter.UserCommonAdapter;
import com.zwn.user.data.model.UserPageCommonItem;
import com.zwn.user.ui.UserCenterViewModel;

public abstract class BaseUserCenterFragment extends Fragment {
    protected static final int DETAIL_BACK_CODE = 1001;
    public ImageView ivUserCommPageDel;
    public ImageView ivUserCommPageDelAll;
    public NetworkErrView nevUserComm;
    public ImageView ivUserCommSaturn;
    public TextView tvUserCommEmpty;
    public LoadingView lvUserCommLoading;
    public TextView tvUserCommonTab;
    protected UserCenterViewModel mViewModel;
    protected boolean mDelMode = false;
    protected boolean mDelAllMode = false;
    protected UserCommonAdapter mAdapter;
    public RecyclerView rvUserCommPage;
    public CenterGridLayoutManager centerGridLayoutManager;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_user_common_page, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel = new ViewModelProvider(requireActivity()).get(UserCenterViewModel.class);
        initView(view);
        initListener();
    }

    public void initView(View view) {
        rvUserCommPage = view.findViewById(R.id.rv_user_comm_page);
        ivUserCommPageDelAll = view.findViewById(R.id.iv_user_comm_page_del_all);
        ivUserCommPageDel = view.findViewById(R.id.iv_user_comm_page_del);
        nevUserComm = view.findViewById(R.id.nev_user_comm);
        ivUserCommSaturn = view.findViewById(R.id.iv_user_comm_saturn);
        tvUserCommEmpty = view.findViewById(R.id.tv_user_comm_empty);
        lvUserCommLoading = view.findViewById(R.id.lv_user_comm_loading);
        tvUserCommonTab = view.findViewById(R.id.tv_user_common_tab);

        ivUserCommPageDelAll.setVisibility(View.INVISIBLE);
        ivUserCommPageDel.setVisibility(View.INVISIBLE);
        nevUserComm.setVisibility(View.INVISIBLE);
        ivUserCommSaturn.setVisibility(View.INVISIBLE);
        tvUserCommEmpty.setVisibility(View.INVISIBLE);
        lvUserCommLoading.startAnim();

        rvUserCommPage.setFocusable(false);
    }

    public void initListener() {

    }

    protected void initOtherListener() {
        ivUserCommPageDel.setOnClickListener(v -> {
            mDelMode = !mDelMode;
            mAdapter.setDelMode(mDelMode);
            ivUserCommPageDel.setImageResource(mDelMode ? R.mipmap.icon_delete_btn_selected : R.mipmap.icon_delete_btn);
        });

        mAdapter.setOnItemClickListener((view, position, commonItem) -> {
            if(mAdapter.getItemCount() > position) {
                UserPageCommonItem item = (UserPageCommonItem) mAdapter.getItem(position);
                try {
                    Intent intent = new Intent(getActivity(), Class.forName("com.zee.launcher.home.ui.detail.DetailActivity"));
                    intent.putExtra("skuId", item.skuId);
                    startActivityForResult(intent, DETAIL_BACK_CODE);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    protected void checkStateMine() {
        if (mAdapter.getItemCount() == 0) {
            mDelMode = true;
            mDelAllMode = true;
            ivUserCommPageDel.performClick();
            ivUserCommPageDel.setVisibility(View.INVISIBLE);
            ivUserCommPageDelAll.setVisibility(View.INVISIBLE);
            ivUserCommSaturn.setVisibility(View.VISIBLE);
            tvUserCommEmpty.setVisibility(View.VISIBLE);
        } else {
            ivUserCommSaturn.setVisibility(View.INVISIBLE);
            tvUserCommEmpty.setVisibility(View.INVISIBLE);
            ivUserCommPageDel.setVisibility(View.VISIBLE);
            ivUserCommPageDelAll.setVisibility(View.VISIBLE);
        }
        tvUserCommonTab.setText(String.format("全部(%d)", mAdapter.getItemCount()));
    }
}
