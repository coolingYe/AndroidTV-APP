package com.zee.setting.activity;

import static com.zee.setting.base.BaseConstants.ZEE_LAUNCHER_PLUGIN_PLAY;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.card.MaterialCardView;
import com.zee.setting.R;
import com.zee.setting.adapter.GuidePagerAdapter;
import com.zee.setting.base.BaseActivity;
import com.zee.setting.base.BaseConstants;
import com.zee.setting.bean.GuideInfo;
import com.zee.setting.config.Config;
import com.zee.setting.data.SettingRepository;
import com.zee.setting.data.SettingViewModel;
import com.zee.setting.data.SettingViewModelFactory;
import com.zee.setting.service.ConnectService;
import com.zee.setting.service.WifiP2pInfoListener;
import com.zee.setting.utils.CommonUtils;
import com.zee.setting.utils.DensityUtils;
import com.zee.setting.utils.DeviceUtils;
import com.zee.setting.utils.DisplayUtil;
import com.zee.setting.utils.EthernetUtils;
import com.zee.setting.utils.Logger;
import com.zee.setting.utils.NetworkUtil;

public class GuideCameraActivity extends BaseActivity implements View.OnFocusChangeListener, WifiP2pInfoListener, ViewTreeObserver.OnGlobalFocusChangeListener {
    private static final String TAG = "GuideCameraActivity";

    private ViewPager2 viewPagerGuide;
    private GuidePagerAdapter guidePagerAdapter;
    private MaterialCardView tvPrevious, tvNext;
    private GuideInfo guideInfo = new GuideInfo();
    private ImageView point2, point3, point4;
    private int currentIndex;
    private View cutView;
    private final int REQUEST_CODE_PERMISSIONS = 1000;
    private final String[] REQUIRED_PERMISSIONS = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION
    };
    private WifiBroadcastReceiver wifiBroadcastReceiver;
    private SettingViewModel settingViewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_guide);

        DensityUtils.autoWidth(getApplication(), this);
        ConnectService.initConnectService(this);
        SettingViewModelFactory factory = new SettingViewModelFactory(SettingRepository.getInstance());
        settingViewModel = new ViewModelProvider(this, factory).get(SettingViewModel.class);

        wifiBroadcastReceiver = new WifiBroadcastReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        registerReceiver(wifiBroadcastReceiver, filter);

        initView();
        initListener();
        requestPermission();

        getWindow().getDecorView().getViewTreeObserver().addOnGlobalFocusChangeListener(this);

    }

    private void initView() {
        point2 = findViewById(R.id.iv_location_2);
        point3 = findViewById(R.id.iv_location_3);
        point4 = findViewById(R.id.iv_location_4);
        cutView = findViewById(R.id.view_cut);
        tvPrevious = findViewById(R.id.card_guide_previous);
        tvNext = findViewById(R.id.card_guide_next);
        viewPagerGuide = findViewById(R.id.view_page_guide);
        guidePagerAdapter = new GuidePagerAdapter(getSupportFragmentManager(), getLifecycle());
        viewPagerGuide.setUserInputEnabled(false);
        viewPagerGuide.setCurrentItem(0);
        viewPagerGuide.setAdapter(guidePagerAdapter);
        currentIndex = 0;
        tvPrevious.setVisibility(View.GONE);
        cutView.setVisibility(View.GONE);
    }

    private void initListener() {
        MaterialCardView ivBack = findViewById(R.id.card_back);
        ivBack.setOnClickListener(v -> finish());
        ivBack.setOnFocusChangeListener(this);
        MaterialCardView ivHelp = findViewById(R.id.card_help);
        ivHelp.setOnClickListener(v -> startActivity(new Intent(this, CameraDescriptionActivity.class)));
        ivHelp.setOnFocusChangeListener(this);
        viewPagerGuide.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                switch (position) {
                    case 0:
                        currentIndex = 0;
                        cutView.setVisibility(View.GONE);
                        tvPrevious.setVisibility(View.GONE);
                        tvNext.requestFocus();
                        ((TextView)findViewById(R.id.tv_guide_next)).setText("下一步");
                        point2.setImageResource(R.mipmap.img_point_unselected);
                        point3.setImageResource(R.mipmap.img_point_unselected);
                        point4.setImageResource(R.mipmap.img_point_unselected);
                        break;
                    case 1:
                        currentIndex = 1;
                        cutView.setVisibility(View.VISIBLE);
                        tvPrevious.setVisibility(View.VISIBLE);
                        tvNext.setVisibility(View.VISIBLE);
                        tvNext.requestFocus();
                        ((TextView)findViewById(R.id.tv_guide_next)).setText("下一步");
                        point2.setImageResource(R.mipmap.img_point_selected);
                        point3.setImageResource(R.mipmap.img_point_unselected);
                        point4.setImageResource(R.mipmap.img_point_unselected);
                        break;
                    case 2:
                        currentIndex = 2;
                        cutView.setVisibility(View.VISIBLE);
                        tvPrevious.setVisibility(View.VISIBLE);
                        tvNext.requestFocus();
                        tvNext.setVisibility(View.VISIBLE);
                        ((TextView)findViewById(R.id.tv_guide_next)).setText("下一步");
                        point3.setImageResource(R.mipmap.img_point_selected);
                        point4.setImageResource(R.mipmap.img_point_unselected);
                        break;
                    case 3:
                        currentIndex = 3;
                        cutView.setVisibility(View.GONE);
                        tvPrevious.setVisibility(View.VISIBLE);
                        tvPrevious.requestFocus();
                        cutView.setVisibility(View.VISIBLE);
                        tvNext.setVisibility(View.VISIBLE);
                        ((TextView)findViewById(R.id.tv_guide_next)).setText("开始体验");
                        point4.setImageResource(R.mipmap.img_point_selected);
                        break;
                }
            }
        });

        tvPrevious.setOnFocusChangeListener(this);
        tvNext.setOnFocusChangeListener(this);
        tvNext.setTag(false);
        tvPrevious.setOnClickListener(v -> viewPagerGuide.setCurrentItem(currentIndex - 1));
        tvNext.setOnClickListener(v -> {
            if (((TextView)findViewById(R.id.tv_guide_next)).getText().equals("开始体验")) {
                if (tvNext.getTag() instanceof Boolean) {
                    if ("com.zee.setting.SHOW_GUIDE_ACTION".equals(getIntent().getAction())) {
                        if ((boolean)tvNext.getTag()) {
                            Intent intent = new Intent(ZEE_LAUNCHER_PLUGIN_PLAY);
                            sendBroadcast(intent);
                        } else showToast("手机摄像头未连接");
                    }
                }
                finish();
                return;
            }
            viewPagerGuide.setCurrentItem(currentIndex + 1);
        });
    }

    public void setConnected(boolean isEnable) {
        tvNext.setTag(isEnable);
    }

    private void requestPermission() {
        boolean hasAllPermission = true;
        for (String permission : REQUIRED_PERMISSIONS) {
            int result = ActivityCompat.checkSelfPermission(this, permission);
            if (PackageManager.PERMISSION_GRANTED != result) {
                hasAllPermission = false;
                break;
            }
        }

        if (hasAllPermission) {
            onPermissionsGranted();
        } else {
            requestPermissions(REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }
    }

    private void onPermissionsGranted() {
        viewPagerGuide.postDelayed(() -> {
            if (ConnectService.getInstance() != null && ConnectService.getInstance().isWifiP2pEnabled()) {
                ConnectService.getInstance().setWifiP2pInfoListener(GuideCameraActivity.this);
                ConnectService.getInstance().requestDeviceInfoAndCreateGroup();
            } else {
                showToast("设备不支持WiFi直连！");
                updateData();
            }
        },1000);
    }

    private void updateData() {
        String ipAddress = null;
        int netWorkType = NetworkUtil.getNetWorkType(this);
        if (netWorkType == NetworkUtil.NETWORK_ETHERNET) {
            ipAddress = EthernetUtils.getIpAddressForInterfaces();
        } else if (netWorkType == NetworkUtil.NETWORK_WIFI) {
            ipAddress = NetworkUtil.getIpAddress(this);
        }

        StringBuilder sbQrcode = new StringBuilder();
        sbQrcode.append(Config.QRCODE_CONTENT_PREFIX).append(";");
        sbQrcode.append(CommonUtils.getDeviceSn()).append(";");
        sbQrcode.append(ipAddress).append(";");
        sbQrcode.append(Config.MESSAGE_SERVER_PORT).append(";");
        sbQrcode.append(DeviceUtils.getWifiMac()).append(";");
        sbQrcode.append("Zee_" + CommonUtils.getDeviceSn().substring(CommonUtils.getDeviceSn().length() - 5));


        GuideInfo.DeviceInfo deviceInfo = new GuideInfo.DeviceInfo();
        deviceInfo.setNetworkName(getConnectedWifiName(this));
        deviceInfo.setDeviceSN(CommonUtils.getDeviceSn());
        deviceInfo.setIpAddress(ipAddress);
        guideInfo.setDeviceInfo(deviceInfo);
        guideInfo.setConnectQRInfo(String.valueOf(sbQrcode));
        settingViewModel.guideInfo.postValue(guideInfo);
    }

    public static String getConnectedWifiName(Context context) {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        if (wifiInfo != null && wifiInfo.getNetworkId() != -1) {
            return wifiInfo.getSSID().replace("\"", "");
        }
        return "null";
    }

    @Override
    public void onFocusChange(View view, boolean hasFocus) {
        MaterialCardView cardView = (view instanceof MaterialCardView) ? (MaterialCardView) view : null;
        if (cardView == null) return;
        if (hasFocus) {
            cardView.setStrokeColor(0xFF4D79FF);
            cardView.setStrokeWidth(DisplayUtil.dip2px(this, 1f));
        } else {
            cardView.setStrokeColor(0x00FFFFFF);
            cardView.setStrokeWidth(0);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ConnectService.getInstance() != null) {
            if(ConnectService.getInstance().isWifiP2pEnabled()){
                updateData();
            }
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
    protected void onDestroy() {
        if (ConnectService.getInstance() != null) {
            ConnectService.getInstance().setWifiP2pInfoListener(null);
            ConnectService.getInstance().setOnWebrtcServerInfoUpdateListener(null);
        }

        if (wifiBroadcastReceiver != null) {
            unregisterReceiver(wifiBroadcastReceiver);
        }
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (ConnectService.getInstance() != null) {
            ConnectService.getInstance().setOnWebrtcServerInfoUpdateListener(null);
            if (ConnectService.getInstance().isWifiP2pEnabled()) {
                ConnectService.getInstance().stopDiscovery();
            }
        }
    }

    @Override
    public void onPeersAvailable(WifiP2pDeviceList wifiP2pDeviceList) {
        runOnUiThread(this::updateData);
    }

    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo) {

    }

    @Override
    public void onDiscoveryStateChanged(int state) {
        runOnUiThread(this::updateData);
    }

    @Override
    public void onUpdateThisDevice(WifiP2pDevice device) {
        runOnUiThread(this::updateData);
    }

    @Override
    public void onGlobalFocusChanged(View oldFocus, View newFocus) {
        Log.d("test", "onGlobalFocusChanged newFocus: " + newFocus);
        Log.d("test", "onGlobalFocusChanged oldFocus: " + oldFocus);
    }

    class WifiBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(intent.getAction())) {
                Logger.i(TAG, "onReceive()==> WIFI_STATE_CHANGED_ACTION");
                updateData();
            } else if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
                Logger.i(TAG, "onReceive() CONNECTIVITY_ACTION");
                ConnectivityManager cm = (ConnectivityManager) context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo wifiNetworkInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                if (wifiNetworkInfo != null && wifiNetworkInfo.isConnected()) {
                    Logger.i(TAG, "onReceive()==> CONNECTIVITY_ACTION, wifi connected!");
                }
                updateData();
            }
        }
    }
}
