package com.zwn.user.ui.user;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.zeewain.base.views.CustomAlertDialog;
import com.zeewain.base.widgets.CenterGridLayoutManager;
import com.zwn.lib_download.model.DownloadInfo;
import com.zwn.user.R;
import com.zwn.user.adapter.UserCommonAdapter;
import com.zwn.user.data.model.UserPageCommonItem;

public class MineDownloadFragment extends BaseUserCenterFragment {

    public static MineDownloadFragment newInstance() {
        return new MineDownloadFragment();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ivUserCommSaturn.setImageResource(R.mipmap.ic_empty_download);
        tvUserCommEmpty.setText("暂无下载，快去体验课件叭~");
    }

    @Override
    public void initView(View view) {
        super.initView(view);
        mAdapter = new UserCommonAdapter();
        mAdapter.setHasStableIds(true);
        centerGridLayoutManager = new CenterGridLayoutManager(requireContext(), 4);
        rvUserCommPage.setLayoutManager(centerGridLayoutManager);
        rvUserCommPage.setAdapter(mAdapter);
    }

    @Override
    public void initListener() {
        super.initListener();
        initOtherListener();
        mAdapter.setOnRemoveItemListener((view, position, commonItem) -> {
            removeDownload(commonItem.skuId);
            checkStateMine();
        });
        mAdapter.setOnItemFocusedListener(integer -> centerGridLayoutManager.smoothScrollToPosition(rvUserCommPage, new RecyclerView.State(), integer));

        ivUserCommPageDelAll.setOnClickListener(v -> {
            mDelAllMode = !mDelAllMode;
            mAdapter.setDelAllMode(mDelAllMode);
            showClearAllDialog(v.getContext());
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        initData();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void initData() {
        mAdapter.clearData();
        mViewModel.getDownloadList();
        for (DownloadInfo downloadInfo: mViewModel.pDownloadInfoList) {
            UserPageCommonItem item = new UserPageCommonItem(downloadInfo.fileImgUrl, downloadInfo.fileName, downloadInfo.extraId);
            mAdapter.addItem(item);
        }
        mAdapter.notifyDataSetChanged();
        checkStateMine();
        lvUserCommLoading.stopAnim();
        lvUserCommLoading.setVisibility(View.INVISIBLE);
    }

    private boolean removeDownload(String skuId) {
        int res = mViewModel.delDownload(skuId);
        if (res > 0) {
            mAdapter.delItemBySkuId(skuId);
            return true;
        } else {
            mViewModel.pToast.setValue("删除失败");
            return false;
        }
    }

    private void showClearAllDialog(Context context){
        final CustomAlertDialog customAlertDialog = new CustomAlertDialog(context);
        customAlertDialog.setTitleText("删除下载");
        customAlertDialog.setMessageText("确定要删除所有下载吗？");
        customAlertDialog.setMessageSummaryText("删除后将无法恢复");
        customAlertDialog.setOnClickListener(new CustomAlertDialog.OnClickListener() {
            @Override
            public void onConfirm(View v) {

            }

            @Override
            public void onPositive(View v) {
                customAlertDialog.dismiss();
                for (int i = mViewModel.pDownloadInfoList.size() - 1; i >= 0; i--) {
                    if (!removeDownload(mViewModel.pDownloadInfoList.get(i).extraId)) {
                        break;
                    }
                }
                checkStateMine();
            }

            @Override
            public void onCancel(View v) {
                customAlertDialog.dismiss();
                mDelAllMode = false;
                mAdapter.setDelAllMode(false);
            }
        });
        customAlertDialog.show();
    }
}
