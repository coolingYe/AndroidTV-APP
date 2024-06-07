package com.zee.device.helper.ui.main;


import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import android.os.Bundle;
import android.provider.Settings;
import android.view.View;

import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.zee.device.base.db.DatabaseController;
import com.zee.device.base.model.DeviceInfo;
import com.zee.device.base.utils.ApkUtil;
import com.zee.device.base.utils.DensityUtils;
import com.zee.device.base.utils.DisplayUtil;
import com.zee.device.base.utils.NetworkUtil;
import com.zee.device.helper.R;
import com.zee.device.helper.adapter.MainDeviceInfoAdapter;
import com.zee.device.helper.data.model.ConnectionState;
import com.zee.device.helper.data.model.ConnectionType;
import com.zee.device.helper.data.model.DataConnectionState;
import com.zee.device.helper.databinding.ActivityMainBinding;
import com.zee.device.base.dialog.CustomAlertDialog;
import com.zee.device.helper.ui.settings.SettingsActivity;
import com.zee.device.helper.widgets.SwipeRecyclerView;
import com.zee.device.home.ui.home.model.LoadState;
import com.zee.device.home.ui.home.upgrade.UpgradeDialogActivity;
import com.zee.device.home.widgets.LinearDividerDecoration;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends MainWiFiP2pActivity implements View.OnClickListener {
    private static final String TAG = "MainActivity";
    private ActivityMainBinding binding;
    private MainDeviceInfoAdapter mainDeviceInfoAdapter;
    private List<DeviceInfo> deviceInfoList = new ArrayList<>();
    private boolean isItemDeleteClick;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewModel.reqHostAppUpgrade(ApkUtil.getAppVersionName(this));
    }

    @Override
    public void initView() {
        DensityUtils.autoWidth(getApplication(), this);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        binding.ivMainSettings.setOnClickListener(this);
        binding.ivMainScanAdd.setOnClickListener(this);
        binding.mcvNetworkSettings.setOnClickListener(this);

        LinearDividerDecoration linearDividerDecoration = new LinearDividerDecoration(LinearLayoutManager.VERTICAL, DisplayUtil.dip2px(this, 12), 0xFFFFFFFF);
        binding.recyclerViewMainDevice.addItemDecoration(linearDividerDecoration);

        deviceInfoList = DatabaseController.instance.getAllDeviceInfo("");
        mainDeviceInfoAdapter = new MainDeviceInfoAdapter(deviceInfoList);
        mainDeviceInfoAdapter.setOnItemClickListener(new MainDeviceInfoAdapter.OnItemClickListener() {
            @Override
            public void onItemLongClick(View view, DeviceInfo deviceInfo) {

            }

            @Override
            public void onItemClick(View view, DeviceInfo deviceInfo) {
                viewModel.addUserActionRecode("link_click", "设备列表");
                if (selectedDeviceInfo != null) {
                    if (selectedDeviceInfo.sn.equals(deviceInfo.sn)) {
                        selectedDeviceInfo = deviceInfo;
                        requestWebrtcPermissions();
                    } else {
                        DataConnectionState dataConnectionState = viewModel.mldConnectionState.getValue();
                        if (dataConnectionState != null && (dataConnectionState.connectionState == ConnectionState.Connecting || dataConnectionState.connectionState == ConnectionState.Connected)) {
                            showOnConnectingDialog(deviceInfo);
                        } else {
                            selectedDeviceInfo = deviceInfo;
                            requestWebrtcPermissions();
                        }
                    }
                } else {
                    selectedDeviceInfo = deviceInfo;
                    requestWebrtcPermissions();
                }
            }
        });
        binding.recyclerViewMainDevice.setAdapter(mainDeviceInfoAdapter);
        binding.recyclerViewMainDevice.setOnMenuStateChangeListener(new SwipeRecyclerView.OnMenuClickListener() {
            @Override
            public void onMenuClick(int position) {
                // 判断是否为连接中或者成功连接
                if (mainDeviceInfoAdapter.connectionLoadState!=null && (mainDeviceInfoAdapter.connectionLoadState.connectionState == ConnectionState.Connected || mainDeviceInfoAdapter.connectionLoadState.connectionState == ConnectionState.Connecting)) {
                    showDeleteFailedDialog();
                    return;
                }
                DeviceInfo deviceInfo = deviceInfoList.get(position);
                mainDeviceInfoAdapter.notifyItemRemoved(position);
                deviceInfoList.remove(position);
                isItemDeleteClick = deviceInfoList.size() != 0;
                DatabaseController.instance.deleteDeviceInfo(deviceInfo.sn);
            }
        });
    }

    @Override
    public void initViewObservable() {
        super.initViewObservable();
        viewModel.mldConnectionState.observe(this, dataConnectionState -> {
            mainDeviceInfoAdapter.updateConnectionLoadState(dataConnectionState);

            if (ConnectionState.ConnectFailed == dataConnectionState.connectionState) {
                if (dataConnectionState.connectionType == ConnectionType.LAN) {
                    showConnectFailedDialog();
                } else {
                    tryLanConnect();
                }
            }
        });

        viewModel.mldHostAppUpgradeState.observe(this, loadState -> {
            if (LoadState.Success == loadState) {
                if (viewModel.hostAppUpgradeResp != null) {
                    UpgradeDialogActivity.showUpgradeTipDialog(this, viewModel.hostAppUpgradeResp);
                }
            }
        });

        viewModel.mldToastMsg.observe(this, msg -> showToast(msg));
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.iv_main_scan_add) {
            if (isWifiConnected) {
                viewModel.addUserActionRecode("scan_click", "设备列表");
                requestCameraPermissions();
            } else {
                showWiFiConnectTipDialog();
            }
        } else if (v.getId() == R.id.mcv_network_settings) {
            Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
            startActivity(intent);
        } else if (v.getId() == R.id.iv_main_settings) {
            Intent intent = new Intent();
            intent.setClass(v.getContext(), SettingsActivity.class);
            startActivity(intent);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        binding.getRoot().postDelayed(new Runnable() {
            @Override
            public void run() {
                isWifiConnected = NetworkUtil.isWifiConnected(getApplicationContext());
                updateUI();
                updateWiFiInfo();
            }
        }, 1000);
    }

    @Override
    public void onDeviceInfoDatabaseChanged() {
        if (!isItemDeleteClick) {
            deviceInfoList = DatabaseController.instance.getAllDeviceInfo("");
            mainDeviceInfoAdapter.updateDataList(deviceInfoList);
        }
        isItemDeleteClick = false;
        updateUI();
    }

    public void onWebrtcPermissionsGranted() {
        /*viewModel.mldConnectionState.setValue(new DataConnectionState(ConnectionState.Connecting));
        sendMsgForStartCall(selectedDeviceInfo.ip, selectedDeviceInfo.port);*/
        super.onWebrtcPermissionsGranted();
    }

    @Override
    public void onConnectivityChanged() {
        updateUI();
        updateWiFiInfo();
    }

    @Override
    public void onWiFiRssiChanged() {
        updateWiFiInfo();
    }

    private void updateUI() {
        if (isWifiConnected) {
            binding.llMainNetworkRoot.setVisibility(View.GONE);
            binding.tvMainDevicesNum.setVisibility(View.VISIBLE);
            binding.tvMainDevicesNum.setText("已添加设备数：" + deviceInfoList.size());
            if (deviceInfoList.size() > 0) {
                binding.llMainNoDeviceRoot.setVisibility(View.GONE);
                binding.recyclerViewMainDevice.setVisibility(View.VISIBLE);
            } else {
                binding.llMainNoDeviceRoot.setVisibility(View.VISIBLE);
                binding.recyclerViewMainDevice.setVisibility(View.GONE);
            }
        } else {
            binding.llMainNetworkRoot.setVisibility(View.VISIBLE);
            binding.tvMainDevicesNum.setVisibility(View.INVISIBLE);
            binding.llMainNoDeviceRoot.setVisibility(View.GONE);
            binding.recyclerViewMainDevice.setVisibility(View.GONE);
        }
    }

    private void updateWiFiInfo() {
        if (isWifiConnected) {
            WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            if (wifiInfo != null && wifiInfo.getBSSID() != null) {
                int level = WifiManager.calculateSignalLevel(wifiInfo.getRssi(), 5);
                if (level == 0) {
                    binding.ivMainWifiLevel.setImageResource(R.mipmap.wifi_level_0);
                } else if (level == 1) {
                    binding.ivMainWifiLevel.setImageResource(R.mipmap.wifi_level_1);
                } else if (level == 2) {
                    binding.ivMainWifiLevel.setImageResource(R.mipmap.wifi_level_2);
                } else {
                    binding.ivMainWifiLevel.setImageResource(R.mipmap.wifi_level_3);
                }
            } else {
                binding.ivMainWifiLevel.setImageResource(R.mipmap.wifi_level_3);
            }
            binding.tvMainWifiState.setText("已连接WiFi");
            binding.tvMainWifiState.setTextColor(0xFF7972FF);
        } else {
            binding.tvMainWifiState.setText("未连接WiFi");
            binding.tvMainWifiState.setTextColor(0xFF999999);
            binding.ivMainWifiLevel.setImageResource(R.mipmap.ic_wifi_status_disconnect);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void showWiFiConnectTipDialog() {
        showMessageDialog("请进行Wifi连入后在尝试扫码","确认");
    }

    private void showOnConnectingDialog(final DeviceInfo deviceInfo) {
        final CustomAlertDialog customAlertDialog = new CustomAlertDialog(this);
        customAlertDialog.setMessageText("您当前有一台设备正在连接\n请确认切换");
        customAlertDialog.setCancelText("取消");
        customAlertDialog.setPositiveText("确认");
        customAlertDialog.setOnClickListener(new CustomAlertDialog.OnClickListener() {
            @Override
            public void onConfirm(View v) {
                customAlertDialog.cancel();
            }

            @Override
            public void onPositive(View v) {
                customAlertDialog.cancel();

                selectedDeviceInfo = deviceInfo;
                requestWebrtcPermissions();
            }

            @Override
            public void onCancel(View v) {
                customAlertDialog.cancel();
            }
        });
        customAlertDialog.show();
    }

    private void showConnectFailedDialog() {
        showMessageDialog("当前设备连接失败请重新尝试","确认");
    }

    private void showDeleteFailedDialog() {
        showMessageDialog("设备连接中或者连接成功\n暂不可删除","确认");
    }

    private void showMessageDialog(String messageText,String confirmTest) {
        final CustomAlertDialog customAlertDialog = new CustomAlertDialog(this);
        customAlertDialog.setMessageText(messageText);
        customAlertDialog.setConfirmText(confirmTest);
        if (messageText.contains("连接失败")) customAlertDialog.showHelpText(true);
        customAlertDialog.setOnClickListener(new CustomAlertDialog.OnClickListener() {
            @Override
            public void onConfirm(View v) {
                customAlertDialog.cancel();
            }

            @Override
            public void onPositive(View v) {
                customAlertDialog.cancel();
            }

            @Override
            public void onCancel(View v) {
                customAlertDialog.cancel();
            }
        });
        customAlertDialog.show();
    }
}