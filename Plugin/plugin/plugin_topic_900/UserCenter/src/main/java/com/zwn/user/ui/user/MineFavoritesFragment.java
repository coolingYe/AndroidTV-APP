package com.zwn.user.ui.user;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.zeewain.base.model.LoadState;
import com.zeewain.base.views.CustomAlertDialog;
import com.zeewain.base.widgets.CenterGridLayoutManager;
import com.zwn.user.R;
import com.zwn.user.adapter.UserCommonAdapter;
import com.zwn.user.data.model.FavoritesItem;
import com.zwn.user.data.model.UserPageCommonItem;

import java.util.ArrayList;
import java.util.List;

public class MineFavoritesFragment extends BaseUserCenterFragment {

    public static MineFavoritesFragment newInstance() {
        return new MineFavoritesFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ivUserCommSaturn.setImageResource(R.mipmap.ic_empty_collect);
        tvUserCommEmpty.setText("暂无收藏，快去收藏课件叭~");
        initObserve();
    }

    @Override
    public void initView(View view) {
        super.initView(view);
        mAdapter = new UserCommonAdapter();
        centerGridLayoutManager = new CenterGridLayoutManager(requireContext(), 5);
        rvUserCommPage.setLayoutManager(centerGridLayoutManager);
        rvUserCommPage.setAdapter(mAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        mViewModel.reqUserFavorites();
    }

    @Override
    public void initListener() {
        super.initListener();
        initOtherListener();
        mAdapter.setOnRemoveItemListener((view, position, commonItem) -> {
            mViewModel.reqDelFavorite(commonItem.skuId);
        });
        mAdapter.setOnItemFocusedListener(integer -> centerGridLayoutManager.smoothScrollToPosition(rvUserCommPage, new RecyclerView.State(), integer));

        ivUserCommPageDelAll.setOnClickListener(v -> {
            mDelAllMode = !mDelAllMode;
            mAdapter.setDelAllMode(mDelAllMode);
            ivUserCommPageDelAll.setImageResource(mDelAllMode ? R.mipmap.icon_delete_all_selected : R.mipmap.icon_delete_all);
            showClearAllDialog(v.getContext());
        });

        nevUserComm.setRetryClickListener(() -> {
            nevUserComm.setVisibility(View.INVISIBLE);
            lvUserCommLoading.setVisibility(View.VISIBLE);
            lvUserCommLoading.startAnim();
            mViewModel.reqUserFavorites();
        });
    }

    private void initObserve() {
        mViewModel.pFavoritesReqState.observe(getViewLifecycleOwner(), state -> {
            lvUserCommLoading.stopAnim();
            lvUserCommLoading.setVisibility(View.INVISIBLE);
            if (state == LoadState.Success) {
                List<UserPageCommonItem> itemList = new ArrayList<>();
                for (FavoritesItem favoritesItem: mViewModel.pFavoriteList) {
                    UserPageCommonItem commonItem = new UserPageCommonItem(favoritesItem.url,
                            favoritesItem.title, favoritesItem.objId);
                    itemList.add(commonItem);
                }
                rvUserCommPage.setVisibility(View.VISIBLE);
                mAdapter.updateItemList(itemList);
                checkStateMine();
            } else {
                nevUserComm.setVisibility(View.VISIBLE);
                rvUserCommPage.setVisibility(View.INVISIBLE);
            }
        });

        mViewModel.mldDelFavoriteReqState.observe(getViewLifecycleOwner(), state -> {
            if (LoadState.Success == state.loadState) {
                mAdapter.delItemBySkuId(state.data);
                checkStateMine();
            }
        });

        mViewModel.pClearFavoritesReqState.observe(getViewLifecycleOwner(), state -> {
            if (state == LoadState.Success) {
                mAdapter.clearData();
                checkStateMine();
            }
        });
    }

    private void showClearAllDialog(Context context){
        final CustomAlertDialog customAlertDialog = new CustomAlertDialog(context);
        customAlertDialog.setMessageText("确定要清空所有收藏吗？");
        customAlertDialog.setMessageSummaryText("清空后无法恢复");
        customAlertDialog.setOnClickListener(new CustomAlertDialog.OnClickListener() {
            @Override
            public void onConfirm(View v) {

            }

            @Override
            public void onPositive(View v) {
                customAlertDialog.dismiss();
                mViewModel.reqClearFavorites();
            }

            @Override
            public void onCancel(View v) {
                customAlertDialog.dismiss();
                mDelAllMode = false;
                mAdapter.setDelAllMode(false);
                ivUserCommPageDelAll.setImageResource(R.mipmap.icon_delete_all);
            }
        });
        customAlertDialog.show();
    }

}
