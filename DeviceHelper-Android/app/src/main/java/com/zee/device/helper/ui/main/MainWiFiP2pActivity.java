package com.zee.device.helper.ui.main;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;

import android.net.NetworkInfo;
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
import android.util.Log;

import androidx.annotation.NonNull;

import com.zee.device.base.model.DeviceInfo;
import com.zee.device.base.utils.Logger;
import com.zee.device.base.utils.NetworkUtil;
import com.zee.device.helper.data.model.ConnectionState;
import com.zee.device.helper.data.model.ConnectionType;

import java.lang.ref.WeakReference;
import java.util.Collection;


public class MainWiFiP2pActivity extends MainWebrtcActivity {
    private static final String TAG = "MainWiFiP2pActivity";
    private final int REQUEST_CODE_WEBRTC_PERMISSIONS = 1200;
    private final String[] REQUIRED_WEBRTC_PERMISSIONS = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) ?
            new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.MODIFY_AUDIO_SETTINGS,
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.NEARBY_WIFI_DEVICES
            } : new String[]{
            Manifest.permission.CAMERA,
            Manifest.permission.MODIFY_AUDIO_SETTINGS,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.RECORD_AUDIO,
    };

    private final IntentFilter intentFilter = new IntentFilter();
    private WiFiDirectBroadcastReceiver wiFiDirectBroadcastReceiver;
    private WifiP2pManager wifiP2pManager;
    private WifiP2pManager.Channel wifiP2pChannel;
    private WifiP2pDevice connectedWifiP2pDevice;
    private boolean isWifiP2pEnabled = false;
    private boolean isDiscoveringMode = false;
    boolean isConnecting = false;
    public DeviceInfo selectedWifiP2pDeviceInfo;

    private static final String MSG_KEY_HOST_IP = "HOST_IP";
    private static final String MSG_KEY_HOST_PORT = "HOST_PORT";
    private static final int MSG_JUDGE_DISCOVERY_NEXT = 1000;
    private static final int MSG_SEND_MSG_START_CALL = 1001;
    private static final int MSG_DIRECT_CONNECTING_PENDING_RESET = 1002;
    private static final int MSG_WIFI_P2P_CONNECTING_OVERTIME = 1005;
    private final MyHandler handler = new MyHandler(Looper.myLooper(), this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION);

        if (initP2p()) {
            registerWiFiDirectReceiver();
        }
    }

    private void registerWiFiDirectReceiver() {
        if (wiFiDirectBroadcastReceiver == null) {
            wiFiDirectBroadcastReceiver = new WiFiDirectBroadcastReceiver(wifiP2pManager, wifiP2pChannel);
        }
        registerReceiver(wiFiDirectBroadcastReceiver, intentFilter);
    }

    private void unregisterWiFiDirectReceiver() {
        if (wiFiDirectBroadcastReceiver != null) {
            unregisterReceiver(wiFiDirectBroadcastReceiver);
        }
    }

    @Override
    protected void onDestroy() {
        unregisterWiFiDirectReceiver();
        handler.removeCallbacksAndMessages(null);
        if (wifiP2pChannel != null) {
            wifiP2pChannel.close();
        }
        super.onDestroy();
    }

    public void requestWebrtcPermissions() {
        requestPermissions(REQUIRED_WEBRTC_PERMISSIONS, REQUEST_CODE_WEBRTC_PERMISSIONS);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_WEBRTC_PERMISSIONS) {
            boolean allPermissionsGranted = true;
            for (int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false;
                    break;
                }
            }
            if (allPermissionsGranted) {
                sendDeviceMsg(selectedDeviceInfo.ip,selectedDeviceInfo.port, MSG_MOBILE_PERMISSION_ENABLE);
                onWebrtcPermissionsGranted();
            } else {
                showToast("请开启权限！");
                sendDeviceMsg(selectedDeviceInfo.ip,selectedDeviceInfo.port, MSG_MOBILE_PERMISSION_ERROR);
            }
        }
    }

    public void onWebrtcPermissionsGranted() {
        dataConnectionState.deviceInfo = selectedDeviceInfo;
        if (isWifiP2pEnabled && wifiP2pChannel != null) {
            dataConnectionState.updateState(ConnectionState.Connecting, ConnectionType.WiFiP2P, "");
            viewModel.mldConnectionState.setValue(dataConnectionState);
            handler.removeMessages(MSG_WIFI_P2P_CONNECTING_OVERTIME);
            handler.sendEmptyMessageDelayed(MSG_WIFI_P2P_CONNECTING_OVERTIME, 12000);

            if (connectedWifiP2pDevice != null) {
                if (!connectedWifiP2pDevice.deviceName.equals(selectedDeviceInfo.name)) {
                    wifiP2pManager.removeGroup(wifiP2pChannel, new WifiP2pManager.ActionListener() {
                        @Override
                        public void onSuccess() {
                            connectedWifiP2pDevice = null;
                            selectedWifiP2pDeviceInfo = selectedDeviceInfo;
                            doDiscoverPeers();
                        }

                        @Override
                        public void onFailure(int i) {
                            connectedWifiP2pDevice = null;
                            selectedWifiP2pDeviceInfo = selectedDeviceInfo;
                            doDiscoverPeers();
                        }
                    });
                } else {
                    selectedWifiP2pDeviceInfo = selectedDeviceInfo;
                    doDiscoverPeers();
                }
            } else {
                selectedWifiP2pDeviceInfo = selectedDeviceInfo;
                doDiscoverPeers();
            }
        } else {
            tryLanConnect();
        }
    }

    public void tryLanConnect() {
        handler.removeMessages(MSG_WIFI_P2P_CONNECTING_OVERTIME);
        selectedWifiP2pDeviceInfo = null;
        dataConnectionState.updateState(ConnectionState.Connecting, ConnectionType.LAN, "");
        viewModel.mldConnectionState.setValue(dataConnectionState);
        sendMsgForStartCall(selectedDeviceInfo.ip, selectedDeviceInfo.port);
    }

    public void onExistCallActivity() {
        selectedWifiP2pDeviceInfo = null;
        registerWiFiDirectReceiver();
        dataConnectionState.updateState(ConnectionState.Init, "");
        viewModel.mldConnectionState.setValue(dataConnectionState);
    }

    public void onEnterCallActivity() {
        handler.removeMessages(MSG_WIFI_P2P_CONNECTING_OVERTIME);
        if (wifiP2pChannel != null && isDiscoveringMode) {
            wifiP2pManager.stopPeerDiscovery(wifiP2pChannel, new WifiP2pManager.ActionListener() {
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
        unregisterWiFiDirectReceiver();

        viewModel.addUserActionRecode("radio_show", "摄像取景页");
    }

    public boolean initP2p() {
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

        wifiP2pManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        if (wifiP2pManager == null) {
            Logger.e(TAG, "Cannot get Wi-Fi Direct system service.");
            return false;
        }

        wifiP2pChannel = wifiP2pManager.initialize(getApplicationContext(), getMainLooper(), null);
        if (wifiP2pChannel == null) {
            Log.e(TAG, "Cannot initialize Wi-Fi Direct.");
            return false;
        }

        return true;
    }

    private void handleJudgeDiscoveryNext() {
        if (connectedWifiP2pDevice != null && isDiscoveringMode) {
            wifiP2pManager.stopPeerDiscovery(wifiP2pChannel, null);
        } else if (connectedWifiP2pDevice == null && !isDiscoveringMode) {
            if (selectedWifiP2pDeviceInfo != null) {
                doDiscoverPeers();
            }
        }
    }

    private void doDiscoverPeers() {
        wifiP2pManager.discoverPeers(wifiP2pChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Logger.d(TAG, "discoverPeers() onSuccess() 正在进行WiFi扫描...");
            }

            @Override
            public void onFailure(int reasonCode) {
                Logger.d(TAG, "discoverPeers() onFailure() WiFi扫描失败！状态码 : " + reasonCode);
                if (reasonCode == 2)
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
            if (wifiP2pChannel != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    if (connectedWifiP2pDevice == null) {
                        wifiP2pManager.requestGroupInfo(wifiP2pChannel, new WifiP2pManager.GroupInfoListener() {
                            @Override
                            public void onGroupInfoAvailable(WifiP2pGroup wifiP2pGroup) {
                                Logger.i(TAG, "onGroupInfoAvailable() wifiP2pGroup=" + wifiP2pGroup);
                                if (wifiP2pGroup != null && !wifiP2pGroup.isGroupOwner() && wifiP2pGroup.getOwner() != null) {
                                    connectedWifiP2pDevice = wifiP2pGroup.getOwner();
                                } else if (wifiP2pGroup != null && wifiP2pGroup.isGroupOwner()) {
                                    wifiP2pManager.removeGroup(wifiP2pChannel, null);
                                }
                            }
                        });
                    }
                }
            }
        } else {
            Logger.i(TAG, "onWifiP2pStateEnabled() false!");
            isConnecting = false;
            connectedWifiP2pDevice = null;
        }
    }

    public void updateThisDevice(WifiP2pDevice device) {
        if (device != null) {
            Logger.i(TAG, "updateThisDevice() device=" + device.deviceName + ", " + device.deviceAddress
                    + ", status=" + NetworkUtil.getWiFiP2pDeviceStatus(device.status));
        }
    }

    public void resetData() {
        Logger.i(TAG, "onDisconnected()");
        isConnecting = false;
        connectedWifiP2pDevice = null;

        if (isWifiP2pEnabled && wifiP2pChannel != null && selectedWifiP2pDeviceInfo != null) {
            wifiP2pManager.requestPeers(wifiP2pChannel, wifiP2pDeviceList -> {//查询是否在INVITED,没有则进行扫描
                Logger.i(TAG, "resetData() onPeersAvailable() wifiP2pDeviceList.size=" + wifiP2pDeviceList.getDeviceList().size());
                Collection<WifiP2pDevice> wifiP2pDevices = wifiP2pDeviceList.getDeviceList();
                boolean isInvitedOrConnected = false;
                for (WifiP2pDevice wifiP2pDevice : wifiP2pDevices) {
                    Logger.i(TAG, "resetData() onPeersAvailable() wifiP2pDevice=" + wifiP2pDevice.deviceName + ", " + wifiP2pDevice.deviceAddress
                            + ", status=" + NetworkUtil.getWiFiP2pDeviceStatus(wifiP2pDevice.status));

                    if (wifiP2pDevice.deviceName.equals(selectedWifiP2pDeviceInfo.name)
                            && (wifiP2pDevice.status == WifiP2pDevice.CONNECTED || wifiP2pDevice.status == WifiP2pDevice.INVITED)) {
                        isInvitedOrConnected = true;
                        break;
                    }
                }

                if (!isInvitedOrConnected) {
                    if (viewModel != null) {
                        dataConnectionState.updateState(ConnectionState.Connecting, "");
                        viewModel.mldConnectionState.setValue(dataConnectionState);
                    }
                    doDiscoverPeers();
                }
            });
        }
    }

    public void onDiscoveryStateChanged(int state) {
        if (state == WifiP2pManager.WIFI_P2P_DISCOVERY_STARTED) {
            Logger.d(TAG, "onDiscoveryStateChanged() state = started");
            isDiscoveringMode = true;
        } else if (state == WifiP2pManager.WIFI_P2P_DISCOVERY_STOPPED) {
            Logger.d(TAG, "onDiscoveryStateChanged() state = stopped");
            isDiscoveringMode = false;
        } else {
            Logger.d(TAG, "onDiscoveryStateChanged() state=" + state);
            isDiscoveringMode = false;
        }
    }

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
        wifiP2pManager.connect(wifiP2pChannel, config, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                Logger.i(TAG, "call connect() onSuccess");
            }

            @Override
            public void onFailure(int reason) {
                Logger.e(TAG, "call connect() onFailure reason=" + reason);
                dataConnectionState.updateState(ConnectionState.ConnectFailed, "发起连接失败！");
                viewModel.mldConnectionState.setValue(dataConnectionState);
                isConnecting = false;
            }
        });
    }

    private final WifiP2pManager.PeerListListener peerListListener = new WifiP2pManager.PeerListListener() {
        @Override
        public void onPeersAvailable(WifiP2pDeviceList wifiP2pDeviceList) {
            Logger.i(TAG, "onPeersAvailable() wifiP2pDeviceList size=" + wifiP2pDeviceList.getDeviceList().size());
            connectedWifiP2pDevice = null;
            Collection<WifiP2pDevice> wifiP2pDevices = wifiP2pDeviceList.getDeviceList();
            for (WifiP2pDevice wifiP2pDevice : wifiP2pDevices) {
                Logger.i(TAG, "onPeersAvailable() wifiP2pDevice=" + wifiP2pDevice.deviceName + ", " + wifiP2pDevice.deviceAddress
                        + ", status=" + NetworkUtil.getWiFiP2pDeviceStatus(wifiP2pDevice.status));

                if (wifiP2pDevice.status == WifiP2pDevice.CONNECTED) {
                    connectedWifiP2pDevice = wifiP2pDevice;
                }

                if (selectedWifiP2pDeviceInfo != null) {
                    if (wifiP2pDevice.deviceName.equals(selectedWifiP2pDeviceInfo.name)) {
                        if (wifiP2pDevice.status == WifiP2pDevice.AVAILABLE) {
                            gotoConnect(wifiP2pDevice);
                        } else if (wifiP2pDevice.status == WifiP2pDevice.CONNECTED) {
                            wifiP2pManager.requestConnectionInfo(wifiP2pChannel, connectionInfoListener);
                        } else if (wifiP2pDevice.status == WifiP2pDevice.INVITED) {
                            Logger.e(TAG, "onPeersAvailable() " + wifiP2pDevice.deviceName + "-----> status invited <-----");
                        }
                    }
                }
            }
        }
    };

    private final WifiP2pManager.ConnectionInfoListener connectionInfoListener = new WifiP2pManager.ConnectionInfoListener() {
        @Override
        public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo) {
            Logger.i(TAG, "onConnectionInfoAvailable() wifiP2pInfo=" + wifiP2pInfo);
            isConnecting = false;
            if (selectedWifiP2pDeviceInfo != null && connectedWifiP2pDevice != null && !wifiP2pInfo.isGroupOwner) {
                if (selectedWifiP2pDeviceInfo.name.equals(connectedWifiP2pDevice.deviceName)) {
                    handler.removeMessages(MSG_WIFI_P2P_CONNECTING_OVERTIME);
                    handler.sendEmptyMessageDelayed(MSG_WIFI_P2P_CONNECTING_OVERTIME, 5000);
                    if (wifiP2pInfo.groupOwnerAddress == null) {
                        handler.removeMessages(MSG_SEND_MSG_START_CALL);
                        sendConnectServerMsg("192.168.49.1", selectedWifiP2pDeviceInfo.port);
                    } else {
                        handler.removeMessages(MSG_SEND_MSG_START_CALL);
                        sendConnectServerMsg(wifiP2pInfo.groupOwnerAddress.getHostAddress(), selectedWifiP2pDeviceInfo.port);
                    }
                }
            }
        }
    };

    private void sendConnectServerMsg(String hostIp, int hostPort) {
        Message message = Message.obtain(handler);
        message.what = MSG_SEND_MSG_START_CALL;
        Bundle bundle = new Bundle();
        bundle.putString(MSG_KEY_HOST_IP, hostIp);
        bundle.putInt(MSG_KEY_HOST_PORT, hostPort);
        message.setData(bundle);
        handler.sendMessageDelayed(message, 500);
    }

    class WiFiDirectBroadcastReceiver extends BroadcastReceiver {
        private static final String TAG = "WiFiDirectBroadcastReceiver";
        private final WifiP2pManager manager;
        private final WifiP2pManager.Channel channel;

        public WiFiDirectBroadcastReceiver(WifiP2pManager manager, WifiP2pManager.Channel channel) {
            super();
            this.manager = manager;
            this.channel = channel;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
                int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
                onWifiP2pStateEnabled(state == WifiP2pManager.WIFI_P2P_STATE_ENABLED);
                Logger.d(TAG, "P2P state changed - " + (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED ? "enable" : "disable"));
            } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {

                // request available peers from the wifi p2p manager. This is an
                // asynchronous call and the calling activity is notified with a
                // callback on PeerListListener.onPeersAvailable()
                if (manager != null && channel != null) {
                    manager.requestPeers(channel, peerListListener);
                }
                Logger.d(TAG, "P2P peers changed");
            } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
                if (manager == null || channel == null) {
                    return;
                }
                Logger.d(TAG, "WIFI_P2P_CONNECTION_CHANGED_ACTION");
                NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
                if (networkInfo.isConnected()) {
                    // we are connected with the other device, request connection info to find group owner IP
                    manager.requestConnectionInfo(channel, connectionInfoListener);
                } else {
                    resetData(); // It's a disconnect
                }
            } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
                updateThisDevice((WifiP2pDevice) intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE));
            } else if (WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION.equals(action)) {
                int state = intent.getIntExtra(WifiP2pManager.EXTRA_DISCOVERY_STATE, 10000);
                onDiscoveryStateChanged(state);
            }
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
                    ((MainWiFiP2pActivity) activity).handleJudgeDiscoveryNext();
                } else if (msg.what == MSG_SEND_MSG_START_CALL) {
                    String hostIp = msg.getData().getString(MSG_KEY_HOST_IP);
                    int hostPort = msg.getData().getInt(MSG_KEY_HOST_PORT);
                    ((MainWiFiP2pActivity) activity).sendMsgForStartCall(hostIp, hostPort);
                } else if (msg.what == MSG_DIRECT_CONNECTING_PENDING_RESET) {
                    ((MainWiFiP2pActivity) activity).isConnecting = false;
                } else if (msg.what == MSG_WIFI_P2P_CONNECTING_OVERTIME) {
                    ((MainWiFiP2pActivity) activity).tryLanConnect();
                }
            }
        }
    }

}