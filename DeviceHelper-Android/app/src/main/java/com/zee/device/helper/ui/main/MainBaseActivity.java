package com.zee.device.helper.ui.main;


import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.net.ConnectivityManager;

import android.net.wifi.WifiManager;

import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;

import com.google.zxing.client.android.Intents;
import com.zee.device.base.config.BaseConstants;
import com.zee.device.base.db.DatabaseController;
import com.zee.device.base.db.DatabaseSettings;
import com.zee.device.base.model.DeviceInfo;
import com.zee.device.base.ui.BaseActivity;
import com.zee.device.base.utils.Logger;
import com.zee.device.base.utils.NetworkUtil;
import com.zee.device.helper.ZeeApplication;
import com.zee.device.helper.ui.capture.ScanQrcodeActivity;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.function.Consumer;

public class MainBaseActivity extends BaseActivity {
    private static final String TAG = "MainBaseActivity";
    private final int REQUEST_CODE_CAMERA_PERMISSION = 1000;
    private CareBroadcastReceiver careBroadcastReceiver;
    public boolean isWifiConnected = false;
    public MainViewModel viewModel;
    private MobileClient mMobileClient;

    public static final  int MSG_MOBILE_PERMISSION_ERROR = 10001;
    public static final  int MSG_MOBILE_PERMISSION_ENABLE = 10003;
    public static final  int MSG_DEVICE_ADDED = 10002;

    private final ActivityResultLauncher<Intent> qrcodeScanLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == Activity.RESULT_OK) {
            Intent data = result.getData();
            if (data != null) {
                String scanResult = data.getStringExtra(Intents.Scan.RESULT);
                handleScanResult(scanResult);
            }
        }
    });

    private final ContentObserver deviceInfoObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            onDeviceInfoDatabaseChanged();
        }
    };

    public void registerObserver() {
        getContentResolver().registerContentObserver(DatabaseSettings.DeviceInfo.CONTENT_URI, true, deviceInfoObserver);
    }

    public void unRegisterObserver() {
        getContentResolver().unregisterContentObserver(deviceInfoObserver);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        initViewObservable();
        registerObserver();
        registerCareReceiver();
    }

    @Override
    protected void onDestroy() {
        qrcodeScanLauncher.unregister();
        unRegisterObserver();
        unregisterCareReceiver();
        super.onDestroy();
    }

    public void requestCameraPermissions() {
        requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CODE_CAMERA_PERMISSION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_CAMERA_PERMISSION) {
            boolean allPermissionsGranted = true;
            for (int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false;
                    break;
                }
            }
            if (allPermissionsGranted) {
                onCameraPermissionsGranted();
            } else {
                showToast("请开启权限！");
            }
        }
    }

    public void sendDeviceMsg(String hostAddress, int port, int action) {
        try {
            String uriString = String.format("ws://%s:%d", hostAddress, port);
            URI uri = new URI(uriString);
            if (mMobileClient != null) {
                mMobileClient.close();
            }
            mMobileClient = new MobileClient(uri);
            mMobileClient.connect();
            mMobileClient.setOpenListener(serverHandshake -> {
                JSONObject message = new JSONObject();
                try {
                    message.put("type", "Mobile");
                    message.put("action", action);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                mMobileClient.send(message.toString());
            });
        } catch (URISyntaxException e) {
            e.printStackTrace();
            Logger.e(TAG, e.toString());
        }
    }

    class MobileClient extends WebSocketClient {

        private Consumer<ServerHandshake> openListener;
        public MobileClient(URI serverUri) {
            super(serverUri, new Draft_6455(), null, 2000);
        }

        public void setOpenListener(Consumer<ServerHandshake> openListener) {
            this.openListener = openListener;
        }

        @Override
        public void onOpen(ServerHandshake handshakedata) {
            if (openListener != null) {
                openListener.accept(handshakedata);
            }
        }

        @Override
        public void onMessage(String message) {
            try {
                JSONObject jsonMessage = new JSONObject(message);
                String type = jsonMessage.getString("type");
                if (type.equals("Mobile")) {
                    mMobileClient.close();
                }
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void onClose(int code, String reason, boolean remote) {

        }

        @Override
        public void onError(Exception ex) {

        }
    }

    public void initView(){
    }

    public void initViewObservable(){

    }

    public void onDeviceInfoDatabaseChanged() {
    }

    public void onCameraPermissionsGranted() {
        gotoScanQrcodeActivity();
    }

    public void onConnectivityChanged() {
    }

    public void onWiFiRssiChanged() {
    }

    public void gotoScanQrcodeActivity() {
        Intent intent = new Intent();
        intent.setAction(Intents.Scan.ACTION);
        intent.setClass(this, ScanQrcodeActivity.class);
        qrcodeScanLauncher.launch(intent);
    }

    private synchronized void handleScanResult(String scanResult) {
        if (!TextUtils.isEmpty(scanResult) && scanResult.startsWith(BaseConstants.QRCODE_CONTENT_PREFIX)) {
            String[] strArray = scanResult.split(";");
            if (strArray.length >= 6) {
                try {
                    DeviceInfo deviceInfo = new DeviceInfo();
                    deviceInfo.sn = strArray[1];
                    deviceInfo.ip = strArray[2];
                    deviceInfo.port = Integer.parseInt(strArray[3]);
                    deviceInfo.mac = strArray[4];
                    deviceInfo.name = strArray[5];

                    DeviceInfo existDeviceInfo = DatabaseController.instance.getDeviceInfoBySN(deviceInfo.sn);
                    if (existDeviceInfo != null) {
                        DatabaseController.instance.updateDeviceInfo(deviceInfo);
                    } else {
                        DatabaseController.instance.addDeviceInfo(deviceInfo);
                    }
                    showToast("添加成功！");
                    sendDeviceMsg(deviceInfo.ip, deviceInfo.port, MSG_DEVICE_ADDED);
                } catch (NumberFormatException ignored) {
                    showToast("不支持该二维码！");
                }
            }
        }
    }

    private void registerCareReceiver() {
        careBroadcastReceiver = new CareBroadcastReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        filter.addAction(WifiManager.RSSI_CHANGED_ACTION);
        filter.addAction(BaseConstants.ACTION_EXIT_WEBRTC_PREVIEW);
        registerReceiver(careBroadcastReceiver, filter);
    }

    private void unregisterCareReceiver() {
        if (careBroadcastReceiver != null) {
            unregisterReceiver(careBroadcastReceiver);
        }
    }

    class CareBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
                Logger.d(TAG, "CONNECTIVITY_ACTION");
                isWifiConnected = NetworkUtil.isWifiConnected(context);
                onConnectivityChanged();
            } else if (WifiManager.RSSI_CHANGED_ACTION.equals(intent.getAction())) {
                onWiFiRssiChanged();
            } else if (BaseConstants.ACTION_EXIT_WEBRTC_PREVIEW.equals(intent.getAction())) {
                ZeeApplication.getInstance().unBindService();
            }
        }
    }
}