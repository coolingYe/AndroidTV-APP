package com.zee.setting.activity;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.zee.setting.R;
import com.zee.setting.base.BaseActivity;
import com.zee.setting.config.Config;
import com.zee.setting.service.ConnectService;
import com.zee.setting.service.WifiP2pInfoListener;
import com.zee.setting.utils.CodeCreator;
import com.zee.setting.utils.CommonUtils;
import com.zee.setting.utils.DeviceUtils;
import com.zee.setting.utils.DisplayUtil;
import com.zee.setting.utils.EthernetUtils;
import com.zee.setting.utils.Logger;
import com.zee.setting.utils.NetworkUtil;


public class ScanConnectionActivity extends BaseActivity implements WifiP2pInfoListener {
    private static final String TAG = "MainActivity";
    private final int REQUEST_CODE_PERMISSIONS = 1000;
    private final String[] REQUIRED_PERMISSIONS = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION
    };
    private ImageView qrcodeImageView;
    private TextView deviceInfoTextView;
    private TextView actionButton;
    private LinearLayout wifiDiscoveryTipLayout;
    private WifiBroadcastReceiver wifiBroadcastReceiver;

    private void requestPermission(){
        boolean hasAllPermission = true;
        for(String permission: REQUIRED_PERMISSIONS){
            int result = ActivityCompat.checkSelfPermission(this, permission);
            if(PackageManager.PERMISSION_GRANTED != result){
                hasAllPermission = false;
                break;
            }
        }

        if(hasAllPermission){
            onPermissionsGranted();
        }else{
            requestPermissions(REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            boolean allPermissionsGranted = true;
            for (int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false;
                    break;
                }
            }
            if (allPermissionsGranted) {
                onPermissionsGranted();
            } else {
                showToast("请开启权限！");
                this.finish();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.i(TAG, "onCreate()");
        ConnectService.initConnectService(this);
        setContentView(R.layout.activity_scan_connection);
        setFinishOnTouchOutside(true);
        qrcodeImageView = findViewById(R.id.iv_qrcode);
        deviceInfoTextView = findViewById(R.id.tv_device_info);
        actionButton = findViewById(R.id.btn_action);
        actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                if(!wifiManager.isWifiEnabled()) {
                    wifiManager.setWifiEnabled(true);
                }else if(ConnectService.getInstance() != null){
                    if(ConnectService.getInstance().getConnectedWifiP2pDevice() != null) {
                        ConnectService.getInstance().disconnectWifiP2p();
                    }else if(ConnectService.getInstance().isDiscoveringMode()){
                        ConnectService.getInstance().stopDiscovery();
                    }else{
                        ConnectService.getInstance().startDiscovery();
                    }
                }
            }
        });

        wifiDiscoveryTipLayout = findViewById(R.id.ll_wifi_discovery_tip);

        wifiBroadcastReceiver = new WifiBroadcastReceiver();
        IntentFilter filter =new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        registerReceiver(wifiBroadcastReceiver, filter);
        requestPermission();
    }

    private void onPermissionsGranted(){
        Logger.i(TAG, "onPermissionsGranted()");
        qrcodeImageView.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(ConnectService.getInstance() != null && ConnectService.getInstance().isWifiP2pEnabled()){
                    ConnectService.getInstance().setWifiP2pInfoListener(ScanConnectionActivity.this);
                    ConnectService.getInstance().requestDeviceInfoAndCreateGroup();
                }else{
                    showToast("设备不支持WiFi直连！");
                    updateView();
                }
            }
        }, 1000);
    }

    private final ConnectService.OnWebrtcServerInfoUpdateListener onWebrtcServerInfoUpdateListener = ip -> {
        Logger.i(TAG, "onUpdate() ip=" + ip);
        runOnUiThread(() -> {
            showToast("无线摄像头连接成功！");
            finish();
        });
    };

    @Override
    protected void onResume() {
        super.onResume();
        Logger.i(TAG, "onResume()");
        if(ConnectService.getInstance() != null) {
            ConnectService.getInstance().setOnWebrtcServerInfoUpdateListener(onWebrtcServerInfoUpdateListener);
            if(ConnectService.getInstance().isWifiP2pEnabled()){
                updateView();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(ConnectService.getInstance() != null) {
            ConnectService.getInstance().setOnWebrtcServerInfoUpdateListener(null);
            if(ConnectService.getInstance().isWifiP2pEnabled()){
                ConnectService.getInstance().stopDiscovery();
            }
        }
    }

    @Override
    protected void onDestroy() {
        Logger.i(TAG, "onDestroy()");
        if(ConnectService.getInstance() != null){
            ConnectService.getInstance().setWifiP2pInfoListener(null);
            ConnectService.getInstance().setOnWebrtcServerInfoUpdateListener(null);
        }

        if(wifiBroadcastReceiver != null){
            unregisterReceiver(wifiBroadcastReceiver);
        }
        super.onDestroy();
    }

    private void updateView(){
        String ipAddress = null;
        int netWorkType = NetworkUtil.getNetWorkType(this);
        if (netWorkType == NetworkUtil.NETWORK_ETHERNET) {
            ipAddress = EthernetUtils.getIpAddressForInterfaces();
        } else if (netWorkType == NetworkUtil.NETWORK_WIFI) {
            ipAddress = NetworkUtil.getIpAddress(this);
        }

        Logger.i(TAG, "updateView() ipAddress=" + ipAddress);
        if(TextUtils.isEmpty(ipAddress)){
            ipAddress = "";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("序列号: ").append(CommonUtils.getDeviceSn()).append("\n");
        sb.append("IP地址: ").append(ipAddress).append("\n");
        sb.append("设备MAC: ").append(DeviceUtils.getWifiMac()).append("\n");

        WifiP2pDevice wifiP2pDevice = null;
        if(ConnectService.getInstance() != null && ConnectService.getInstance().isWifiP2pEnabled()) {
            wifiP2pDevice = ConnectService.getInstance().getSelfWifiP2pDevice();
            if(wifiP2pDevice == null){
                sb.append("设备不支持WiFi直连！\n").append("\n");
            }else{
                sb.append("设备名称: ").append(wifiP2pDevice.deviceName).append("\n");
            }

            WifiP2pDevice connectedWifiP2pDevice = ConnectService.getInstance().getConnectedWifiP2pDevice();
            if (connectedWifiP2pDevice != null) {
                sb.append("WiFi直连: ").append(connectedWifiP2pDevice.deviceName).append(" 已连接").append("\n");
                actionButton.setText("断开WiFi直连");
                actionButton.setVisibility(View.VISIBLE);
                wifiDiscoveryTipLayout.setVisibility(View.GONE);
            }else{
                if(ConnectService.getInstance().isDiscoveringMode()){
                    actionButton.setText("关闭WiFi直连");
                    wifiDiscoveryTipLayout.setVisibility(View.VISIBLE);
                }else{
                    actionButton.setText("开启WiFi直连");
                    wifiDiscoveryTipLayout.setVisibility(View.GONE);
                }
                actionButton.setVisibility(View.VISIBLE);
            }
        }else{
            sb.append("设备不支持WiFi直连！\n").append("\n");

            WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            if(!wifiManager.isWifiEnabled()) {
                actionButton.setText("开启WiFi");
                actionButton.setVisibility(View.VISIBLE);
            }else{
                actionButton.setVisibility(View.GONE);
            }
            wifiDiscoveryTipLayout.setVisibility(View.GONE);
        }

        deviceInfoTextView.setText(sb.toString());

        if(TextUtils.isEmpty(ipAddress) && wifiP2pDevice == null) {
            return;
        }

        StringBuilder sbQrcode = new StringBuilder();
        sbQrcode.append(Config.QRCODE_CONTENT_PREFIX).append(";");
        sbQrcode.append(CommonUtils.getDeviceSn()).append(";");
        sbQrcode.append(ipAddress).append(";");
        sbQrcode.append(Config.MESSAGE_SERVER_PORT).append(";");
        sbQrcode.append(DeviceUtils.getWifiMac()).append(";");
        if(wifiP2pDevice != null) {
            sbQrcode.append(wifiP2pDevice.deviceName);
        }else {
            sbQrcode.append("Zee_" + CommonUtils.getDeviceSn().substring(CommonUtils.getDeviceSn().length() - 5));
        }

        int width = DisplayUtil.dip2px(this, 160);
        int height = DisplayUtil.dip2px(this, 160);
        qrcodeImageView.setImageBitmap(CodeCreator.createQRCode(sbQrcode.toString(), width, height, null));

    }

    /*private void showToast(String msg){
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }*/

    @Override
    public void onPeersAvailable(WifiP2pDeviceList wifiP2pDeviceList) {
        runOnUiThread(() -> updateView());
    }

    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo) {

    }

    @Override
    public void onDiscoveryStateChanged(int state) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                updateView();
            }
        });

    }

    @Override
    public void onUpdateThisDevice(WifiP2pDevice device) {
        runOnUiThread(() -> updateView());
    }

    class WifiBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if(WifiManager.WIFI_STATE_CHANGED_ACTION.equals(intent.getAction())){
                Logger.i(TAG, "onReceive()==> WIFI_STATE_CHANGED_ACTION");
                updateView();
            }else if(ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())){
                Logger.i(TAG, "onReceive() CONNECTIVITY_ACTION");
                ConnectivityManager cm = (ConnectivityManager) context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo wifiNetworkInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                if (wifiNetworkInfo != null && wifiNetworkInfo.isConnected()) {
                    Logger.i(TAG, "onReceive()==> CONNECTIVITY_ACTION, wifi connected!");
                }
                updateView();
            }
        }
    }
}