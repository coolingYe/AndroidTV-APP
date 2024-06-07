package com.zee.device.home.ui.device;

import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;


import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.zee.device.base.config.SharePrefer;
import com.zee.device.base.db.DatabaseController;
import com.zee.device.base.db.DatabaseSettings;
import com.zee.device.base.model.DeviceInfo;
import com.zee.device.base.ui.BaseActivity;
import com.zee.device.base.utils.DisplayUtil;
import com.zee.device.base.utils.SPUtils;
import com.zee.device.base.widgets.CustomActionBar;
import com.zee.device.home.R;
import com.zee.device.home.ui.device.adapter.DeviceInfoAdapter;
import com.zee.device.home.widgets.LinearDividerDecoration;

import java.util.List;

public class DeviceListActivity extends BaseActivity {

    private boolean isDelMode = false;
    private DeviceInfoAdapter deviceInfoAdapter;
    private CustomActionBar customActionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_list);

        customActionBar = findViewById(R.id.cab_device_list);
        String deviceSn = SPUtils.getInstance().getString(SharePrefer.SELECTED_DEVICE_SN);
        RecyclerView deviceInfoRecyclerView = findViewById(R.id.recycler_view_device_info);
        LinearDividerDecoration linearDividerDecoration = new LinearDividerDecoration(LinearLayoutManager.VERTICAL, DisplayUtil.dip2px(this, 1), 0xFFF5F5FF);
        linearDividerDecoration.setVerticalPadding(DisplayUtil.dip2px(this, 20), DisplayUtil.dip2px(this, 19));
        deviceInfoRecyclerView.addItemDecoration(linearDividerDecoration);

        List<DeviceInfo> dataList = DatabaseController.instance.getAllDeviceInfo("");
        deviceInfoAdapter = new DeviceInfoAdapter(dataList, deviceSn);
        deviceInfoRecyclerView.setAdapter(deviceInfoAdapter);

        if(dataList.size() > 0) {
            customActionBar.setFilterVisibility(View.VISIBLE);
            customActionBar.setFilterImage(R.mipmap.icon_delete_unselect);
            customActionBar.setOnFilterImageClickListener(view -> {
                isDelMode = !isDelMode;
                if (isDelMode) {
                    customActionBar.setFilterImage(R.mipmap.icon_delete_selected);
                } else {
                    customActionBar.setFilterImage(R.mipmap.icon_delete_unselect);
                }
                deviceInfoAdapter.setDelMode(isDelMode);
            });
        }

        registerObserver();
    }

    private final ContentObserver deviceInfoObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            List<DeviceInfo> dataList = DatabaseController.instance.getAllDeviceInfo("");
            deviceInfoAdapter.updateDataList(dataList);
            if(dataList.size() == 0) {
                isDelMode = false;
                customActionBar.setFilterImage(R.mipmap.icon_delete_unselect);
                customActionBar.setFilterVisibility(View.GONE);
            }
        }
    };

    public void registerObserver() {
        getContentResolver().registerContentObserver(DatabaseSettings.DeviceInfo.CONTENT_URI, true, deviceInfoObserver);
    }

    public void unRegisterObserver() {
        getContentResolver().unregisterContentObserver(deviceInfoObserver);
    }

    @Override
    protected void onDestroy() {
        unRegisterObserver();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if(isDelMode){
            isDelMode = false;
            customActionBar.setFilterImage(R.mipmap.icon_delete_unselect);
            deviceInfoAdapter.setDelMode(isDelMode);
            return;
        }
        super.onBackPressed();
    }
}