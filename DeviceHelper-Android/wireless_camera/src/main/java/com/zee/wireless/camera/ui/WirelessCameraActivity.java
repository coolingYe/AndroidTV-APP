package com.zee.wireless.camera.ui;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;


import com.zee.device.base.config.BaseConstants;
import com.zee.device.base.db.DatabaseController;
import com.zee.device.base.dialog.LoadingDialog;
import com.zee.device.base.model.DeviceInfo;
import com.zee.device.base.ui.BaseActivity;
import com.zee.device.base.utils.Logger;
import com.zee.device.base.utils.NetworkUtil;
import com.zee.wireless.camera.R;
import com.zee.wireless.camera.receiver.WiFiDirectBroadcastReceiver;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;

public class WirelessCameraActivity extends BaseActivity implements WifiP2pManager.ChannelListener, WifiP2pManager.PeerListListener, WifiP2pManager.ConnectionInfoListener  {

    private static final String TAG = "MainActivity";
    private final int REQUEST_CODE_PERMISSIONS = 1200;
    private final int REQUEST_CODE_SCAN_RESULT = 1201;
    private final int REQUEST_CODE_CALL_ACTIVITY_RESULT = 1202;

    private final String[] REQUIRED_PERMISSIONS = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) ?
            new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.MODIFY_AUDIO_SETTINGS,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.NEARBY_WIFI_DEVICES
            } : new String[]{
            Manifest.permission.CAMERA,
            Manifest.permission.MODIFY_AUDIO_SETTINGS,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.RECORD_AUDIO,
    };

    private MessageClient mMessageClient;

    private final IntentFilter intentFilter = new IntentFilter();
    private WiFiDirectBroadcastReceiver wiFiDirectBroadcastReceiver;
    private WifiP2pManager manager;
    private WifiP2pManager.Channel channel;
    private WifiP2pDevice connectedWifiP2pDevice;
    private boolean isWifiP2pEnabled = false;

    private LoadingDialog loadingDialog;
    private DeviceInfo selectedWifiP2pDeviceInfo;
    private DeviceInfo selectedLanDeviceInfo;
    private DeviceInfo selectedDeviceInfo;
    private boolean isDiscoveringMode = false;

    private static final String MSG_KEY_HOST_IP = "HOST_IP";
    private static final String MSG_KEY_HOST_PORT = "HOST_PORT";
    private static final int MSG_JUDGE_DISCOVERY_NEXT = 1000;
    private static final int MSG_SEND_MSG_START_CALL = 1001;
    private static final int MSG_DIRECT_CONNECTING_PENDING_RESET = 1002;
    private final MyHandler handler = new MyHandler(Looper.myLooper(), this);

    public void showLoadingDialog() {
        if (loadingDialog == null) {
            loadingDialog = new LoadingDialog(this);
        }
        loadingDialog.showLoading();
        loadingDialog.setLoadingTip("正在连接...");
    }

    public void hideLoadingDialog() {
        if (loadingDialog != null) {
            loadingDialog.hideLoading();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.i(TAG, "onCreate()");
        setContentView(R.layout.activity_wireless_camera);

        Bundle bundle = getIntent().getBundleExtra(BaseConstants.EXTRA_DEVICE_INFO);
        if(bundle == null){
            finish();
            return;
        }
        selectedDeviceInfo = (DeviceInfo) bundle.getSerializable(BaseConstants.EXTRA_DEVICE_INFO);

        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION);

        TextView tvConnectCameraLan = findViewById(R.id.tv_connect_camera_lan);
        tvConnectCameraLan.setOnClickListener(v -> {
            Logger.i(TAG, "onLanClick() deviceInfo=" + selectedDeviceInfo);
            selectedWifiP2pDeviceInfo = null;
            if(connectedWifiP2pDevice != null && channel != null){
                manager.removeGroup(channel, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        connectedWifiP2pDevice = null;
                    }

                    @Override
                    public void onFailure(int i) {}
                });
            }
            selectedLanDeviceInfo = selectedDeviceInfo;
            onLanClicked();
        });

        TextView tvConnectCameraDirect = findViewById(R.id.tv_connect_camera_direct);
        tvConnectCameraDirect.setOnClickListener(v -> {
            Logger.i(TAG, "onWiFiDirectClick() deviceInfo=" + selectedDeviceInfo);
            selectedLanDeviceInfo = null;
            if(connectedWifiP2pDevice != null && channel != null){
                if(!connectedWifiP2pDevice.deviceName.equals(selectedDeviceInfo.name)){
                    manager.removeGroup(channel, new WifiP2pManager.ActionListener() {
                        @Override
                        public void onSuccess() {
                            connectedWifiP2pDevice = null;
                            selectedWifiP2pDeviceInfo = selectedDeviceInfo;
                            onWiFiDirectClicked();
                        }

                        @Override
                        public void onFailure(int i) {}
                    });
                }else{
                    selectedWifiP2pDeviceInfo = selectedDeviceInfo;
                    onWiFiDirectClicked();
                }
            }else{
                selectedWifiP2pDeviceInfo = selectedDeviceInfo;
                onWiFiDirectClicked();
            }
        });

        requestPermission();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Logger.i(TAG, "onResume()");
        wiFiDirectBroadcastReceiver = new WiFiDirectBroadcastReceiver(manager, channel, this);
        registerReceiver(wiFiDirectBroadcastReceiver, intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Logger.i(TAG, "onPause()");
        unregisterReceiver(wiFiDirectBroadcastReceiver);

        if(channel != null && isDiscoveringMode) {
            manager.stopPeerDiscovery(channel, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    Logger.i(TAG, "stopPeerDiscovery() onSuccess");
                }

                @Override
                public void onFailure(int code) {
                    Logger.i(TAG, "stopPeerDiscovery() onFailure code=" + code);
                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        Logger.i(TAG, "onDestroy()");
        handler.removeCallbacksAndMessages(null);
        hideLoadingDialog();
        loadingDialog = null;
        if(channel != null) {
            channel.close();
        }
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(REQUEST_CODE_CALL_ACTIVITY_RESULT == requestCode){
            selectedLanDeviceInfo = null;
            selectedWifiP2pDeviceInfo = null;
        }
    }

    private void requestPermission() {
        requestPermissions(REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
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
                onPermissionsGrantedDone();
            } else {
                showToast("请开启权限！");
                this.finish();
            }
        }
    }

    private void onPermissionsGrantedDone(){
        Logger.i(TAG, "onPermissionsGrantedDone()");
        if (initP2p()) {

        }
    }

    private boolean initP2p() {
        // Device capability definition check
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_WIFI_DIRECT)) {
            Logger.e(TAG, "Wi-Fi Direct is not supported by this device.");
            return false;
        }

        // Hardware capability check
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifiManager == null) {
            Logger.e(TAG, "Cannot get Wi-Fi system service.");
            return false;
        }

        if (!wifiManager.isP2pSupported()) {
            Logger.e(TAG, "Wi-Fi Direct is not supported by the hardware or Wi-Fi is off.");
            return false;
        }

        manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        if (manager == null) {
            Logger.e(TAG, "Cannot get Wi-Fi Direct system service.");
            return false;
        }

        channel = manager.initialize(getApplicationContext(), getMainLooper(), null);
        if (channel == null) {
            Log.e(TAG, "Cannot initialize Wi-Fi Direct.");
            return false;
        }

        return true;
    }

    private void handleJudgeDiscoveryNext(){
        if(connectedWifiP2pDevice != null && isDiscoveringMode){
            manager.stopPeerDiscovery(channel, null);
        }else if(connectedWifiP2pDevice == null && !isDiscoveringMode){
            if(selectedWifiP2pDeviceInfo != null || selectedLanDeviceInfo == null){
                doDiscoverPeers();
            }
        }
    }

    private void onLanClicked(){
        if(NetworkUtil.isNetworkAvailable(this)) {
            if (selectedLanDeviceInfo != null && !TextUtils.isEmpty(selectedLanDeviceInfo.ip)) {
                showLoadingDialog();
                sendMsgForStartCall(selectedLanDeviceInfo.ip, selectedLanDeviceInfo.port);
            }
        }else{
            showToast("网络未连接！");
        }
    }

    @SuppressLint("MissingPermission")
    private void onWiFiDirectClicked(){
        if(isWifiP2pEnabled && channel != null){
            showLoadingDialog();
            doDiscoverPeers();
        }
    }

    private void doDiscoverPeers(){
        manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Logger.d(TAG, "discoverPeers() onSuccess() 正在进行WiFi扫描...");
            }

            @Override
            public void onFailure(int reasonCode) {
                Logger.d(TAG, "discoverPeers() onFailure() WiFi扫描失败！状态码 : " + reasonCode);
                if(reasonCode == 2)
                    showToast("WiFi扫描正忙！");
                else
                    showToast("WiFi扫描失败！ state=" + reasonCode);
            }
        });
    }

    @SuppressLint("MissingPermission")
    public void onWifiP2pStateEnabled(boolean isWifiP2pEnabled) {
        this.isWifiP2pEnabled = isWifiP2pEnabled;
        if (isWifiP2pEnabled) {
            if(channel != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    manager.requestDeviceInfo(channel, new WifiP2pManager.DeviceInfoListener() {
                        @Override
                        public void onDeviceInfoAvailable(@Nullable WifiP2pDevice wifiP2pDevice) {
                            if (wifiP2pDevice != null) {
                                updateThisDevice(wifiP2pDevice);
                            }
                        }
                    });

                    if(connectedWifiP2pDevice == null){
                        manager.requestGroupInfo(channel, new WifiP2pManager.GroupInfoListener() {
                            @Override
                            public void onGroupInfoAvailable(WifiP2pGroup wifiP2pGroup) {
                                Logger.i(TAG, "onGroupInfoAvailable() wifiP2pGroup=" + wifiP2pGroup);
                                if(wifiP2pGroup != null && !wifiP2pGroup.isGroupOwner() && wifiP2pGroup.getOwner() != null){
                                    connectedWifiP2pDevice = wifiP2pGroup.getOwner();
                                }else if(wifiP2pGroup != null && wifiP2pGroup.isGroupOwner()){
                                    manager.removeGroup(channel, null);
                                }
                            }
                        });
                    }
                }
            }
        }else{
            Logger.i(TAG,"onWifiP2pStateEnabled() false!");
            resetData();
        }

    }

    public void updateThisDevice(WifiP2pDevice device) {
        Logger.i(TAG, "updateThisDevice() device=" + device.deviceName + ", " + device.deviceAddress
                + ", status=" + NetworkUtil.getWiFiP2pDeviceStatus(device.status));
    }

    public void resetData() {
        Logger.i(TAG, "onDisconnected()");
        isConnecting = false;
        connectedWifiP2pDevice = null;

        if(isWifiP2pEnabled && channel != null && selectedWifiP2pDeviceInfo != null){
            manager.requestPeers(channel, wifiP2pDeviceList -> {
                Logger.i(TAG, "resetData() onPeersAvailable() wifiP2pDeviceList.size=" + wifiP2pDeviceList.getDeviceList().size());
                Collection<WifiP2pDevice> wifiP2pDevices = wifiP2pDeviceList.getDeviceList();
                boolean isInvitedOrConnected = false;
                for (WifiP2pDevice wifiP2pDevice : wifiP2pDevices) {
                    Logger.i(TAG, "resetData() onPeersAvailable() wifiP2pDevice=" + wifiP2pDevice.deviceName + ", " + wifiP2pDevice.deviceAddress
                            + ", status=" + NetworkUtil.getWiFiP2pDeviceStatus(wifiP2pDevice.status));

                    if(wifiP2pDevice.deviceName.equals(selectedWifiP2pDeviceInfo.name)
                            && (wifiP2pDevice.status == WifiP2pDevice.CONNECTED || wifiP2pDevice.status == WifiP2pDevice.INVITED)){
                        isInvitedOrConnected = true;
                        break;
                    }
                }

                if(!isInvitedOrConnected){
                    hideLoadingDialog();
                    showLoadingDialog();
                    doDiscoverPeers();
                }
            });
        }
    }

    @SuppressLint("MissingPermission")
    public void onDiscoveryStateChanged(int state){
        if (state == WifiP2pManager.WIFI_P2P_DISCOVERY_STARTED){
            Logger.d(TAG, "onDiscoveryStateChanged() state = started");
            isDiscoveringMode = true;
        }else if(state == WifiP2pManager.WIFI_P2P_DISCOVERY_STOPPED){
            Logger.d(TAG, "onDiscoveryStateChanged() state = stopped");
            isDiscoveringMode = false;
            /*if(connectedWifiP2pDevice == null && channel != null) {
                doDiscoverPeers();
            }*/
        }else{
            Logger.d(TAG, "onDiscoveryStateChanged() state=" + state);
            isDiscoveringMode = false;
        }
    }

    @Override
    public void onChannelDisconnected() {
        Logger.i(TAG, "onChannelDisconnected()");
        connectedWifiP2pDevice = null;
        isConnecting = false;
    }

    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo) {
        Logger.i(TAG, "onConnectionInfoAvailable() wifiP2pInfo=" + wifiP2pInfo);
        isConnecting = false;
        if(selectedWifiP2pDeviceInfo !=null && connectedWifiP2pDevice != null && !wifiP2pInfo.isGroupOwner){
            if(selectedWifiP2pDeviceInfo.name.equals(connectedWifiP2pDevice.deviceName)) {
                if(wifiP2pInfo.groupOwnerAddress == null){
                    handler.removeMessages(MSG_SEND_MSG_START_CALL);
                    sendConnectServerMsg("192.168.49.1", selectedWifiP2pDeviceInfo.port);
                }else{
                    handler.removeMessages(MSG_SEND_MSG_START_CALL);
                    sendConnectServerMsg(wifiP2pInfo.groupOwnerAddress.getHostAddress(), selectedWifiP2pDeviceInfo.port);
                }
            }
        }
    }

    private void sendConnectServerMsg(String hostIp, int hostPort){
        Message message = Message.obtain(handler);
        message.what = MSG_SEND_MSG_START_CALL;
        Bundle bundle = new Bundle();
        bundle.putString(MSG_KEY_HOST_IP, hostIp);
        bundle.putInt(MSG_KEY_HOST_PORT, hostPort);
        message.setData(bundle);
        handler.sendMessageDelayed(message, 500);
    }

    @Override
    public void onPeersAvailable(WifiP2pDeviceList wifiP2pDeviceList) {
        Logger.i(TAG, "onPeersAvailable() wifiP2pDeviceList size=" + wifiP2pDeviceList.getDeviceList().size());
        connectedWifiP2pDevice = null;
        Collection<WifiP2pDevice> wifiP2pDevices = wifiP2pDeviceList.getDeviceList();
        for (WifiP2pDevice wifiP2pDevice : wifiP2pDevices) {
            Logger.i(TAG, "onPeersAvailable() wifiP2pDevice=" + wifiP2pDevice.deviceName + ", " + wifiP2pDevice.deviceAddress
                    + ", status=" + NetworkUtil.getWiFiP2pDeviceStatus(wifiP2pDevice.status));

            if(wifiP2pDevice.status == WifiP2pDevice.CONNECTED){
                connectedWifiP2pDevice = wifiP2pDevice;
            }

            if(selectedWifiP2pDeviceInfo != null) {
                if (wifiP2pDevice.deviceName.equals(selectedWifiP2pDeviceInfo.name)) {
                    if(wifiP2pDevice.status == WifiP2pDevice.AVAILABLE) {
                        gotoConnect(wifiP2pDevice);
                    }else if(wifiP2pDevice.status == WifiP2pDevice.CONNECTED){
                        manager.requestConnectionInfo(channel, WirelessCameraActivity.this);
                    }else if(wifiP2pDevice.status == WifiP2pDevice.INVITED){
                        Logger.e(TAG, "onPeersAvailable() " + wifiP2pDevice.deviceName + "-----> status invited <-----");
                    }
                }
            }
        }
    }

    boolean isConnecting = false;
    @SuppressLint("MissingPermission")
    private synchronized void gotoConnect(WifiP2pDevice wifiP2pDevice) {
        if (isConnecting) {
            Logger.i(TAG, "gotoConnect() pending isConnecting = true");
            return;
        }
        isConnecting = true;
        handler.removeMessages(MSG_DIRECT_CONNECTING_PENDING_RESET);
        handler.sendEmptyMessageDelayed(MSG_DIRECT_CONNECTING_PENDING_RESET, 5000);
        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = wifiP2pDevice.deviceAddress;
        config.wps.setup = WpsInfo.PBC;
        config.groupOwnerIntent = 0;
        manager.connect(channel, config, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                Logger.i(TAG, "call connect() onSuccess");
            }

            @Override
            public void onFailure(int reason) {
                showToast("Connect failed. Retry.");
                Logger.e(TAG, "call connect() onFailure reason=" + reason);
                hideLoadingDialog();
                isConnecting = false;
            }
        });
    }

    private void sendMsgForStartCall(String hostAddress, int port){
        try {
            Logger.i(TAG, "sendMsgForStartCall() getHostAddress=" + hostAddress);
            @SuppressLint("DefaultLocale")
            String uriString = String.format("ws://%s:%d", hostAddress, port);
            URI uri = new URI(uriString);
            if (mMessageClient != null) {
                mMessageClient.close();
            }
            mMessageClient = new MessageClient(uri);
            mMessageClient.connect();
        } catch (URISyntaxException e) {
            e.printStackTrace();
            Logger.e(e.toString());
            hideLoadingDialog();
        }
    }

    private void nextToCallActivity(){
        hideLoadingDialog();
        Intent intent = new Intent();
        intent.setClass(this, CallActivity.class);
        startActivityForResult(intent, REQUEST_CODE_CALL_ACTIVITY_RESULT);
    }

    class MessageClient extends WebSocketClient {

        public MessageClient(URI serverUri) {
            super(serverUri);
        }

        @Override
        public void onOpen(ServerHandshake handshake) {
            try {
                String localIp = null;
                if(mMessageClient.getLocalSocketAddress() != null && mMessageClient.getLocalSocketAddress().getAddress() != null){
                    localIp = mMessageClient.getLocalSocketAddress().getAddress().getHostAddress();
                }
                if(localIp == null){
                    localIp = NetworkUtil.getLocalIpAddress();
                }
                Logger.d("=== MessageClient onOpen() localIp=" + localIp);
                if(localIp != null) {
                    JSONObject message = new JSONObject();
                    message.put("type", "WebrtcServerInfo");
                    message.put("ip", localIp);
                    message.put("port", BaseConstants.WEBRTC_SIGNAL_SERVER_PORT);
                    mMessageClient.send(message.toString());
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onMessage(final String message) {
            Logger.d("=== MessageClient onMessage(): message="+message);
            try {
                JSONObject jsonMessage = new JSONObject(message);
                String type = jsonMessage.getString("type");
                if (type.equals("WebrtcServerInfo")) {
                    mMessageClient.close();
                    nextToCallActivity();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onClose(int code, String reason, boolean remote) {
            Logger.d("=== MessageClient onClose(): reason="+reason+", remote="+remote);
        }

        @Override
        public void onError(Exception ex) {
            ex.printStackTrace();
            Logger.d("=== MessageClient onError() ex="+ex.getMessage());
            hideLoadingDialog();
        }
    }

    public static class MyHandler extends Handler {
        private final WeakReference<Activity> mActivity;

        public MyHandler(Looper looper, Activity activity) {
            super(looper);
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Activity activity = mActivity.get();
            if (activity != null) {
                if (msg.what == MSG_JUDGE_DISCOVERY_NEXT) {
                    ((WirelessCameraActivity) activity).handleJudgeDiscoveryNext();
                } else if(msg.what == MSG_SEND_MSG_START_CALL){
                    String hostIp = msg.getData().getString(MSG_KEY_HOST_IP);
                    int hostPort = msg.getData().getInt(MSG_KEY_HOST_PORT);
                    ((WirelessCameraActivity) activity).sendMsgForStartCall(hostIp, hostPort);
                } else if(msg.what == MSG_DIRECT_CONNECTING_PENDING_RESET){
                    ((WirelessCameraActivity) activity).isConnecting = false;
                }
            }
        }
    }
}