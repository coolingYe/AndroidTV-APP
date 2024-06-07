package com.zwn.user.ui.user;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.zeewain.base.config.BaseConstants;
import com.zeewain.base.utils.NetworkUtil;
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
        ivUserCommSaturn.setImageResource(R.mipmap.img_no_data_download);
        tvUserCommEmpty.setText("暂无下载，快去下载课件叭~");
    }

    @Override
    public void initView(View view) {
        super.initView(view);
        mAdapter = new UserCommonAdapter();
        rvUserCommPage = view.findViewById(R.id.rv_user_comm_page);
        centerGridLayoutManager = new CenterGridLayoutManager(requireContext(), 5);
        rvUserCommPage.setLayoutManager(centerGridLayoutManager);
        rvUserCommPage.setAdapter(mAdapter);

        MaterialCardView cardCacheAll = view.findViewById(R.id.card_user_comm_page_cache_all);
        cardCacheAll.setVisibility(View.VISIBLE);
        cardCacheAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(BaseConstants.START_CACHE_MANAGER_ACTION);
                v.getContext().sendBroadcast(intent);
            }
        });
        cardCacheAll.setOnFocusChangeListener(this);
    }

    @Override
    public void initListener() {
        super.initListener();
        initOtherListener();
        mAdapter.setOnRemoveItemListener((view, position, commonItem) -> {
            if(NetworkUtil.isNetworkAvailable(view.getContext())) {
                removeDownload(commonItem.skuId);
                checkStateMine();
            }else{
                mViewModel.pToast.setValue("删除失败，请检查网络状态!");
            }
        });
        mAdapter.setOnItemFocusedListener(integer -> centerGridLayoutManager.smoothScrollToPosition(rvUserCommPage, new RecyclerView.State(), integer));

        cardUserCommPageDelAll.setOnClickListener(v -> {
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
            if (!TextUtils.isEmpty(downloadInfo.extraId)){
                UserPageCommonItem item = new UserPageCommonItem(downloadInfo.fileImgUrl, downloadInfo.fileName, downloadInfo.extraId);
                mAdapter.addItem(item);
            }

        }
        mAdapter.notifyDataSetChanged();
        checkStateMine();
        lvUserCommLoading.stopAnim();
        lvUserCommLoading.setVisibility(View.INVISIBLE);
    }

    private boolean removeDownload(String skuId) {
        if (getContext() != null) {
            int res = mViewModel.delDownload(getContext(), skuId);
            if (res > 0) {
                mAdapter.delItemBySkuId(skuId);
                return true;
            } else {
                mViewModel.pToast.setValue("删除失败");
                return false;
            }
        }
        return false;
    }

    private void showClearAllDialog(Context context){
        final CustomAlertDialog customAlertDialog = new CustomAlertDialog(context);
        customAlertDialog.setMessageText("确定要删除全部吗？");
        customAlertDialog.setMessageSummaryText("同时删除文件");
        customAlertDialog.setOnClickListener(new CustomAlertDialog.OnClickListener() {
            @Override
            public void onConfirm(View v) {

            }

            @Override
            public void onPositive(View v) {
                customAlertDialog.dismiss();
                if(NetworkUtil.isNetworkAvailable(v.getContext())) {
                    for (int i = mViewModel.pDownloadInfoList.size() - 1; i >= 0; i--) {
                        if (!removeDownload(mViewModel.pDownloadInfoList.get(i).extraId)) {
                            break;
                        }
                    }

                    if (mAdapter.getItemCount() == 0) {
                        mViewModel.delAllDownloadPluginApp();
                    }
                    checkStateMine();
                }else{
                    mViewModel.pToast.setValue("删除失败，请检查网络状态!");
                }
            }

            @Override
            public void onCancel(View v) {
                customAlertDialog.dismiss();
            }
        });
        customAlertDialog.show();
    }

    protected void checkStateMine() {
        if (mAdapter.getItemCount() == 0) {
            rvUserCommPage.setVisibility(View.GONE);
        } else {
            rvUserCommPage.setVisibility(View.VISIBLE);
        }
        super.checkStateMine();
    }
}
