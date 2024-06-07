package com.zee.setting.service;

import static com.zee.setting.base.BaseConstants.ZEE_SETTINGS_CAMERA_STATE;
import static com.zee.setting.config.Config.PERSIST_DATA_WEBRTC_SERVER_INFO;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.zee.setting.bean.CameraBean;
import com.zee.setting.cache.SPUtils;
import com.zee.setting.cache.SharePrefer;
import com.zee.setting.camera2.CameraUtil;
import com.zee.setting.config.Config;
import com.zee.setting.utils.CommonUtils;
import com.zee.setting.utils.Logger;
import com.zee.setting.utils.NetworkUtil;
import com.zee.setting.utils.SystemProperties;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import io.reactivex.functions.Consumer;

public class ConnectService extends Service implements WifiP2pManager.ChannelListener, WifiP2pManager.PeerListListener, WifiP2pManager.ConnectionInfoListener {
    private static final String TAG = "ConnectService";
    private static ConnectService instance = null;
    private WebSocket mRemoteCameraClient;
    private String mRemoteClientName;
    //    private SignalClient mClient;
    private WiFiDirectBroadcastReceiver wiFiDirectBroadcastReceiver;
    private WifiP2pManager manager;
    private WifiP2pManager.Channel channel;
    private WifiP2pDevice selfWifiP2pDevice;
    private WifiP2pDevice connectedWifiP2pDevice;
    private boolean isWifiP2pEnabled = false;
    private boolean isDiscoveringMode = false;
    private WifiP2pInfoListener wifiP2pInfoListener;
    private boolean enableDiscovery = false;
    private OnWebrtcServerInfoUpdateListener onWebrtcServerInfoUpdateListener;
    private OnCameraEventListener onCameraEventListener;
    private Consumer<Boolean> onUsbDiskEventListener;

    private static final int MSG_JUDGE_DISCOVERY_NEXT = 1000;
    public static final int MSG_USE_CAMERA_ATTACHED = 11000;
    public static final int MSG_USE_CAMERA_DETACHED = 11001;
    public static final int MSG_REMOTE_CAMERA_ATTACHED = 11002;
    public static final int MSG_REMOTE_CAMERA_DETACHED = 11003;
    public static final int MSG_CHECK_CAMERA_STATUS = 11004;
    public static final int MSG_USB_DISK_ATTACHED = 11005;
    public static final int MSG_USB_DISK_DETACHED = 11006;
    @SuppressLint("HandlerLeak")
    private final Handler mHandler = new Handler(Looper.myLooper()) {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MSG_JUDGE_DISCOVERY_NEXT) {
                handleJudgeDiscoveryNext();
            } else if (msg.what == MSG_USE_CAMERA_ATTACHED) {
                Logger.i(TAG, "handleMessage()  attached usbCamera");
                handleCameraEvent(true, true);
            } else if (msg.what == MSG_USE_CAMERA_DETACHED) {
                Logger.i(TAG, "handleMessage()  detached usbCamera");
                if (isDetachedUsed()) {
                    handleCameraEvent(true, true);
                } else {
                    if (onCameraEventListener != null) {
                        onCameraEventListener.onCameraUpdate(false);
                    }
                }
            } else if (msg.what == MSG_REMOTE_CAMERA_ATTACHED) {
                Logger.i(TAG, "handleMessage()  attached remoteCamera");
                handleCameraEvent(false, true);
            } else if (msg.what == MSG_REMOTE_CAMERA_DETACHED) {
                Logger.i(TAG, "handleMessage()  detached remoteCamera");
                if (isDetachedUsed()) {
                    handleCameraEvent(false, true);
                } else {
                    if (onCameraEventListener != null) {
                        onCameraEventListener.onCameraUpdate(false);
                    }
                }
            } else if (msg.what == MSG_CHECK_CAMERA_STATUS) {
                Logger.i(TAG, "handleMessage()  check camera status");
                handleCameraEvent(false, false);
            } else if (msg.what == MSG_USB_DISK_ATTACHED) {
                if (onUsbDiskEventListener != null) {
                    try {
                        onUsbDiskEventListener.accept(true);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            } else if (msg.what == MSG_USB_DISK_DETACHED) {
                if (onUsbDiskEventListener != null) {
                    try {
                        onUsbDiskEventListener.accept(false);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    };

    private boolean isDetachedUsed() {//是否拔掉了正在使用的
        String cameraCache = SPUtils.getInstance().getString(SharePrefer.CameraSelected);
        if (!TextUtils.isEmpty(cameraCache)) {
            CameraBean cameraBeanCache = new Gson().fromJson(cameraCache, CameraBean.class);
            List<CameraBean> cameraBeanList = getCameraBeanList();

            CameraBean usedCameraBean = null;
            for (CameraBean cameraBean : cameraBeanList) {
                if (cameraBeanCache.getCameraId().equals(cameraBean.cameraId)) {
                    usedCameraBean = cameraBean;
                    break;
                }
            }

            if (usedCameraBean == null) {
                return true;
            }
        }

        return false;
    }

    private void handleCameraEvent(boolean isUsbCamera, boolean isRealEvent) {
        Logger.i(TAG, "handleCameraEvent()");
        List<CameraBean> cameraBeanList = getCameraBeanList();
        if (cameraBeanList.size() >= 2) {
            String cameraCache = SPUtils.getInstance().getString(SharePrefer.CameraSelected);
            CameraBean selectedCameraBean = null;
            if (!TextUtils.isEmpty(cameraCache)) {
                CameraBean cameraBeanCache = new Gson().fromJson(cameraCache, CameraBean.class);
                for (CameraBean cameraBean : cameraBeanList) {
                    if (cameraBeanCache.getCameraId().equals(cameraBean.cameraId)) {
                        selectedCameraBean = cameraBean;
                        break;
                    }
                }
            }

            if (selectedCameraBean == null) {
                for (CameraBean cameraBean : cameraBeanList) {
                    if (cameraBean.getType() == CameraBean.TYPE_USB_CAMERA) {
                        SPUtils.getInstance().put(SharePrefer.CameraSelected, new Gson().toJson(cameraBean));
                        selectedCameraBean = cameraBean;
                        break;
                    }
                }
            }

            if (selectedCameraBean == null) {
                for (CameraBean cameraBean : cameraBeanList) {
                    if (cameraBean.getType() == CameraBean.TYPE_REMOTE_CAMERA) {
                        SPUtils.getInstance().put(SharePrefer.CameraSelected, new Gson().toJson(cameraBean));
                        selectedCameraBean = cameraBean;
                        break;
                    }
                }
            }

            assert selectedCameraBean != null;
            CommonUtils.saveGlobalCameraId(selectedCameraBean.cameraId);
            if (isRealEvent) {
                CommonUtils.startSettingsActivity(ConnectService.this);
            }
        } else if (cameraBeanList.size() == 1) {
            CameraBean cameraBean = cameraBeanList.get(0);
            SPUtils.getInstance().put(SharePrefer.CameraSelected, new Gson().toJson(cameraBean));
            CommonUtils.saveGlobalCameraId(cameraBean.cameraId);
            /*if (isRealEvent) {
                CommonUtils.startSettingsActivity(ConnectService.this);
            }*/
        } else {
            SPUtils.getInstance().put(SharePrefer.CameraSelected, "");
            CommonUtils.saveGlobalCameraId("");
        }

        if (isRealEvent && onCameraEventListener != null) {
            onCameraEventListener.onCameraUpdate(isUsbCamera);
        }
    }

    public ConnectService() {
        Logger.i(TAG, "ConnectService(), ConnectService in construction!");
    }

    public static ConnectService getInstance() {
        return instance;
    }

    public static void initConnectService(Context context) {
        Logger.i(TAG, "startConnectService()");
        Intent startServiceIntent = new Intent(context.getApplicationContext(), ConnectService.class);
        context.getApplicationContext().startService(startServiceIntent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Logger.i(TAG, "onCreate()");
        instance = this;
        MessageServer mMessageServer = new MessageServer(Config.MESSAGE_SERVER_PORT);
        mMessageServer.setReuseAddr(true);
        mMessageServer.start();

        registerUSBCameraReceiver();
        mHandler.sendEmptyMessageDelayed(MSG_CHECK_CAMERA_STATUS, 6000);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (channel == null) {
            if (initP2p()) {
                wiFiDirectBroadcastReceiver = new WiFiDirectBroadcastReceiver(manager, channel, this);
                IntentFilter intentFilter = new IntentFilter();
                intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
                intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
                intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
                intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
                intentFilter.addAction(WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION);
                intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
                registerReceiver(wiFiDirectBroadcastReceiver, intentFilter);
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        mHandler.removeCallbacksAndMessages(null);
        unregisterUSBCameraReceiver();
        if (wiFiDirectBroadcastReceiver != null) {
            unregisterReceiver(wiFiDirectBroadcastReceiver);
        }
        super.onDestroy();
    }

    public WifiP2pDevice getSelfWifiP2pDevice() {
        return selfWifiP2pDevice;
    }

    public WifiP2pDevice getConnectedWifiP2pDevice() {
        return connectedWifiP2pDevice;
    }

    public boolean isWifiP2pEnabled() {
        return isWifiP2pEnabled;
    }

    public boolean isDiscoveringMode() {
        return isDiscoveringMode;
    }

    public void disconnectWifiP2p() {
        manager.removeGroup(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Logger.e(TAG, "removeGroup() onSuccess");
            }

            @Override
            public void onFailure(int code) {
                Logger.e(TAG, "removeGroup() onFailure code=" + code);
            }
        });
    }

    public void setDeviceName(String devName) {
        try {
            Class[] paramTypes = new Class[3];
            paramTypes[0] = WifiP2pManager.Channel.class;
            paramTypes[1] = String.class;
            paramTypes[2] = WifiP2pManager.ActionListener.class;
            Method setDeviceName = manager.getClass().getMethod("setDeviceName", paramTypes);
            setDeviceName.setAccessible(true);
            Object argList[] = new Object[3];
            argList[0] = channel;
            argList[1] = devName;
            argList[2] = new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    Logger.i(TAG, "setDeviceName succeeded");
                }

                @Override
                public void onFailure(int reason) {
                    Logger.e(TAG, "setDeviceName Failed" + reason);
                }
            };
            setDeviceName.invoke(manager, argList);
        } catch (Exception e) {
            Logger.e(TAG, e.getMessage());
            e.printStackTrace();
        }
    }

    public void setWifiP2pInfoListener(WifiP2pInfoListener wifiP2pInfoListener) {
        this.wifiP2pInfoListener = wifiP2pInfoListener;
    }

    public void startDiscovery() {
        enableDiscovery = true;
        mHandler.removeMessages(MSG_JUDGE_DISCOVERY_NEXT);
        mHandler.sendEmptyMessageDelayed(MSG_JUDGE_DISCOVERY_NEXT, 0);
    }

    public void stopDiscovery() {
        enableDiscovery = false;
        mHandler.removeMessages(MSG_JUDGE_DISCOVERY_NEXT);
        if (channel != null && isDiscoveringMode) {
            manager.stopPeerDiscovery(channel, null);
        }
    }

    @SuppressLint("MissingPermission")
    public void requestDeviceInfoAndCreateGroup() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            manager.requestDeviceInfo(channel, device -> {
                if (device != null) {
                    updateThisDevice(device);
                } else {
                    Logger.e(TAG, "onDeviceInfoAvailable() wifiP2pDevice null");
                }
            });

            manager.requestGroupInfo(channel, new WifiP2pManager.GroupInfoListener() {
                @Override
                public void onGroupInfoAvailable(WifiP2pGroup wifiP2pGroup) {
                    Logger.i(TAG, "onGroupInfoAvailable() wifiP2pGroup=" + wifiP2pGroup);
                    if (wifiP2pGroup != null) {
                        if (!wifiP2pGroup.isGroupOwner()) {
                            manager.removeGroup(channel, new WifiP2pManager.ActionListener() {
                                @Override
                                public void onSuccess() {
                                    Logger.i(TAG, "removeGroup() onSuccess");
                                    createGroup();
                                }

                                @Override
                                public void onFailure(int code) {
                                    Logger.e(TAG, "removeGroup() onFailure code=" + code);
                                    createGroup();
                                }
                            });
                        } else {
                            if (connectedWifiP2pDevice == null && wifiP2pGroup.getClientList().size() > 0) {
                                connectedWifiP2pDevice = wifiP2pGroup.getClientList().stream().findFirst().get();
                            }
                        }

                    } else {
                        createGroup();
                    }
                }
            });

            manager.requestNetworkInfo(channel, new WifiP2pManager.NetworkInfoListener() {
                @Override
                public void onNetworkInfoAvailable(@NonNull NetworkInfo networkInfo) {
                    Logger.i(TAG, "onNetworkInfoAvailable() isConnected=" + networkInfo.isConnected());
                }
            });
        }
    }

    @SuppressLint("MissingPermission")
    private void createGroup() {
        manager.createGroup(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Logger.i(TAG, "*** createGroup() onSuccess ***");
            }

            @Override
            public void onFailure(int code) {
                Logger.e(TAG, "createGroup() onFailure code=" + code);
            }
        });
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

        channel = manager.initialize(this, getMainLooper(), null);
        if (channel == null) {
            Log.e(TAG, "Cannot initialize Wi-Fi Direct.");
            return false;
        }

        return true;
    }

    public void onWifiP2pStateEnabled(boolean isWifiP2pEnabled) {
        this.isWifiP2pEnabled = isWifiP2pEnabled;
        if (isWifiP2pEnabled) {
            requestDeviceInfoAndCreateGroup();
            setDeviceName("Zee_" + CommonUtils.getDeviceSn().substring(CommonUtils.getDeviceSn().length() - 5));
        } else {
            connectedWifiP2pDevice = null;
            selfWifiP2pDevice = null;
        }
    }

    public void updateThisDevice(WifiP2pDevice device) {
        selfWifiP2pDevice = device;
        Logger.i(TAG, "updateThisDevice() wifiP2pDevice=" + device.deviceName + ", " + device.deviceAddress
                + ", status=" + NetworkUtil.getWiFiP2pDeviceStatus(device.status));

        if (wifiP2pInfoListener != null) {
            wifiP2pInfoListener.onUpdateThisDevice(selfWifiP2pDevice);
        }
    }

    public void onDiscoveryStateChanged(int state) {
        if (state == WifiP2pManager.WIFI_P2P_DISCOVERY_STARTED) {
            Logger.d(TAG, "onDiscoveryStateChanged()==========>>>>> state = started");
            isDiscoveringMode = true;
        } else if (state == WifiP2pManager.WIFI_P2P_DISCOVERY_STOPPED) {
            Logger.d(TAG, "onDiscoveryStateChanged()==========>>>>> state = stopped");
            isDiscoveringMode = false;
            if (connectedWifiP2pDevice == null && channel != null) {
                doDiscoverPeers();
            }
        } else {
            Logger.d(TAG, "onDiscoveryStateChanged()==========>>>>> state=" + state);
            isDiscoveringMode = false;
        }

        if (wifiP2pInfoListener != null) {
            wifiP2pInfoListener.onDiscoveryStateChanged(state);
        }
    }

    public void onDisconnected() {
        Logger.i(TAG, "onDisconnected()");
        connectedWifiP2pDevice = null;
        mHandler.removeMessages(MSG_JUDGE_DISCOVERY_NEXT);
        mHandler.sendEmptyMessageDelayed(MSG_JUDGE_DISCOVERY_NEXT, 1000);
    }

    @Override
    public void onChannelDisconnected() {
        Logger.i(TAG, "onChannelDisconnected()");
    }

    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo) {
        Logger.i(TAG, "onConnectionInfoAvailable() " + wifiP2pInfo.toString());
        if (!wifiP2pInfo.isGroupOwner) {
            SystemProperties.set(PERSIST_DATA_WEBRTC_SERVER_INFO, wifiP2pInfo.groupOwnerAddress.getHostAddress() + ";" + Config.WEBRTC_SIGNAL_SERVER_PORT);
        }
        if (wifiP2pInfoListener != null) {
            wifiP2pInfoListener.onConnectionInfoAvailable(wifiP2pInfo);
        }
    }

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
        }

        if (wifiP2pInfoListener != null) {
            wifiP2pInfoListener.onPeersAvailable(wifiP2pDeviceList);
        }

        mHandler.removeMessages(MSG_JUDGE_DISCOVERY_NEXT);
        mHandler.sendEmptyMessageDelayed(MSG_JUDGE_DISCOVERY_NEXT, 1000);
    }

    private void handleJudgeDiscoveryNext() {
        if (connectedWifiP2pDevice != null && isDiscoveringMode) {
            manager.stopPeerDiscovery(channel, null);
        } else if (connectedWifiP2pDevice == null && !isDiscoveringMode) {
            doDiscoverPeers();
        }
    }

    @SuppressLint("MissingPermission")
    private void doDiscoverPeers() {
        if (enableDiscovery) {
            manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    Logger.d(TAG, "discoverPeers() onSuccess() 正在进行WiFi扫描...");
                }

                @Override
                public void onFailure(int reasonCode) {
                    Logger.d(TAG, "discoverPeers() onFailure() WiFi扫描失败！状态码 : " + reasonCode);
                    if (WifiP2pManager.BUSY == reasonCode) {
                        mHandler.removeMessages(MSG_JUDGE_DISCOVERY_NEXT);
                        mHandler.sendEmptyMessageDelayed(MSG_JUDGE_DISCOVERY_NEXT, 1000);
                    }
                }
            });
        }
    }

    static class WiFiDirectBroadcastReceiver extends BroadcastReceiver {
        private final WifiP2pManager manager;
        private final WifiP2pManager.Channel channel;
        private final ConnectService connectService;

        public WiFiDirectBroadcastReceiver(WifiP2pManager manager, WifiP2pManager.Channel channel, ConnectService connectService) {
            super();
            this.manager = manager;
            this.channel = channel;
            this.connectService = connectService;
        }

        @SuppressLint("MissingPermission")
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
                int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
                Logger.d(TAG, "onReceive() P2P state changed, enable=" + (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED));
                connectService.onWifiP2pStateEnabled(state == WifiP2pManager.WIFI_P2P_STATE_ENABLED);
            } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
                Logger.d(TAG, "onReceive() P2P peers changed");
                if (manager != null) {
                    manager.requestPeers(channel, connectService);
                }
            } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
                Logger.d(TAG, "onReceive() P2P connection changed");
                if (manager == null) {
                    return;
                }
                NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
                if (networkInfo.isConnected()) {
                    // we are connected with the other device, request connection info to find group owner IP
                    manager.requestConnectionInfo(channel, connectService);
                } else {
                    connectService.onDisconnected(); // It's a disconnect
                }
            } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
                connectService.updateThisDevice((WifiP2pDevice) intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE));
            } else if (WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION.equals(action)) {
                int state = intent.getIntExtra(WifiP2pManager.EXTRA_DISCOVERY_STATE, 10000);
                connectService.onDiscoveryStateChanged(state);
            } else if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action)) {
                Logger.d(TAG, "onReceive() NetConnection changed");
            }
        }
    }

    private USBCameraReceiver usbCameraReceiver;

    private void registerUSBCameraReceiver() {
        usbCameraReceiver = new USBCameraReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        intentFilter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        registerReceiver(usbCameraReceiver, intentFilter);
    }

    private void unregisterUSBCameraReceiver() {
        if (usbCameraReceiver != null) {
            unregisterReceiver(usbCameraReceiver);
        }
    }

    class USBCameraReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
                UsbDevice usbDevice = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                Logger.i(TAG, "onReceive()  attached usbDevice " + usbDevice);
                if (usbDevice != null)
                    Logger.i(TAG, "onReceive()====>  attached usbDevice " + usbDevice.getProductName() + " " + usbDevice.getManufacturerName());
                if (CameraUtil.isUsbCameraDevice(usbDevice)) {
                    Logger.i(TAG, "onReceive()====> total camera = " + CameraUtil.getCameraNum(context));
                    mHandler.removeMessages(MSG_USE_CAMERA_ATTACHED);
                    mHandler.removeMessages(MSG_USE_CAMERA_DETACHED);
                    mHandler.sendEmptyMessageDelayed(MSG_USE_CAMERA_ATTACHED, 2500);
                }
                if (CameraUtil.isUsbStorageDevice(usbDevice)) {
                    new Handler(Looper.getMainLooper()).post(ConnectService.this::showUSBDiskDialog);
                }
            } else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                UsbDevice usbDevice = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                Logger.i(TAG, "onReceive()  detached usbDevice " + usbDevice);
                if (usbDevice != null)
                    Logger.i(TAG, "onReceive()<====  detached usbDevice " + usbDevice.getProductName() + " " + usbDevice.getManufacturerName());
                if (CameraUtil.isUsbCameraDevice(usbDevice)) {
                    Logger.i(TAG, "onReceive()<==== total camera = " + CameraUtil.getCameraNum(context));
                    mHandler.removeMessages(MSG_USE_CAMERA_ATTACHED);
                    mHandler.removeMessages(MSG_USE_CAMERA_DETACHED);
                    mHandler.sendEmptyMessageDelayed(MSG_USE_CAMERA_DETACHED, 2500);
                }

                if (CameraUtil.isUsbStorageDevice(usbDevice)) {
                    if (usbDiskDialog != null) {
                        if (usbDiskDialog.isShowing()) {
                            usbDiskDialog.cancel();
                        }
                    }
                }
            }
        }
    }

    private AlertDialog usbDiskDialog;

    private void showUSBDiskDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext());
        builder.setTitle("检测到USB存储设备")
                .setMessage("是否打开文件夹")
                .setPositiveButton("打开", (dialog, which) -> {
                    Intent launchIntent = getPackageManager().getLaunchIntentForPackage("com.android.documentsui");
                    if (launchIntent != null) {
                        startActivity(launchIntent);
                    } else {
                        // 应用程序未安装或包名无效
                        Toast.makeText(this, "打开失败", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("取消", (dialog, which) -> {

                });
        usbDiskDialog = builder.create();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {//8.0新特性
            usbDiskDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
        } else {
            usbDiskDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        }
        usbDiskDialog.show();
    }

    class MessageServer extends WebSocketServer {

        public MessageServer(int port) {
            super(new InetSocketAddress(port));
        }

        @Override
        public void onOpen(WebSocket conn, ClientHandshake handshake) {
            Logger.d(TAG, "MessageServer onOpen() LocalSocketAddress=" + conn.getLocalSocketAddress() + ", RemoteSocketAddress=" + conn.getRemoteSocketAddress());
            /*if (mWebSocketClient != null) {
                mWebSocketClient.close();
            }
            mWebSocketClient = conn;
            if (conn.getRemoteSocketAddress() != null && (conn.getRemoteSocketAddress().getAddress() != null)) {
                String remoteHostAddress = conn.getRemoteSocketAddress().getAddress().getHostAddress();
                SystemProperties.set(PERSIST_DATA_WEBRTC_SERVER_INFO, remoteHostAddress + ";" + Config.WEBRTC_SIGNAL_SERVER_PORT);
            }*/
        }

        @Override
        public void onClose(WebSocket conn, int code, String reason, boolean remote) {
            Logger.d(TAG, "MessageServer onClose() reason=" + reason + ", remote=" + remote);
            if (mRemoteCameraClient != null && mRemoteCameraClient.hashCode() == conn.hashCode()) {
                mRemoteCameraClient = null;
                mHandler.removeMessages(MSG_REMOTE_CAMERA_DETACHED);
                mHandler.sendEmptyMessageDelayed(MSG_REMOTE_CAMERA_DETACHED, 1000);
            }
        }

        @Override
        public void onMessage(WebSocket conn, String message) {
            Logger.d(TAG, "MessageServer onMessage() message=" + message);
            try {
                JSONObject jsonMessage = new JSONObject(message);
                String type = jsonMessage.getString("type");
                if (type.equals("WebrtcServerInfo")) {
                    String ip = jsonMessage.getString("ip");
                    int port = jsonMessage.getInt("port");
                    SystemProperties.set(PERSIST_DATA_WEBRTC_SERVER_INFO, ip + ";" + port);

                    if (onWebrtcServerInfoUpdateListener != null) {
                        onWebrtcServerInfoUpdateListener.onUpdate(ip);
                    }

                    JSONObject respMessage = new JSONObject();
                    respMessage.put("type", "WebrtcServerInfo");
                    conn.send(respMessage.toString());
                } else if (type.equals("ZeeDevHelperClientInfo")) {
                    if (mRemoteCameraClient != null && mRemoteCameraClient.isOpen() && mRemoteCameraClient.hashCode() != conn.hashCode()) {
                        JSONObject respMessage = new JSONObject();
                        respMessage.put("type", "KickOut");
                        mRemoteCameraClient.send(respMessage.toString());
                        mRemoteCameraClient.close();
                    }
                    String ip = jsonMessage.getString("ip");
                    int port = jsonMessage.getInt("port");
                    SystemProperties.set(PERSIST_DATA_WEBRTC_SERVER_INFO, ip + ";" + port);
                    mRemoteClientName = jsonMessage.getString("deviceName");
                    mRemoteCameraClient = conn;
                    mHandler.removeMessages(MSG_REMOTE_CAMERA_ATTACHED);
                    mHandler.sendEmptyMessageDelayed(MSG_REMOTE_CAMERA_ATTACHED, 1000);
                } else if (type.equals("Mobile")) {
                    int action = jsonMessage.getInt("action");
                    Logger.d(TAG, String.valueOf(action));
                    sendOtherChange(action);

                    JSONObject respMessage2 = new JSONObject();
                    respMessage2.put("type", "Mobile");
                    conn.send(respMessage2.toString());
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onError(WebSocket conn, Exception ex) {
            ex.printStackTrace();
            Logger.e(TAG, "MessageServer onMessage() ex=" + ex.getMessage());
        }

        @Override
        public void onStart() {
            Logger.d(TAG, "MessageServer onStart()");
            setConnectionLostTimeout(100);
        }
    }

    public boolean isExistRemoteCamera() {
        return mRemoteCameraClient != null && mRemoteCameraClient.isOpen();
    }

    private String getRemoteClientName() {
        if (mRemoteClientName == null || mRemoteClientName.isEmpty()) {
            return "手机摄像头";
        }
        return mRemoteClientName;
    }

    public List<CameraBean> getCameraBeanList() {
        List<CameraBean> cameraBeanList = new ArrayList<>();
        String[] cameraIdList = CameraUtil.getCameraIds(this);
        if (cameraIdList != null && cameraIdList.length > 0) {
            for (String s : cameraIdList) {
                cameraBeanList.add(new CameraBean("摄像头" + s, s, CameraBean.TYPE_USB_CAMERA));
            }
        }

        if (ConnectService.getInstance() != null && ConnectService.getInstance().isExistRemoteCamera()) {
            cameraBeanList.add(new CameraBean(getRemoteClientName(), Config.REMOTE_CAMERA_UID, CameraBean.TYPE_REMOTE_CAMERA));
        }

        return cameraBeanList;
    }

    public void setOnWebrtcServerInfoUpdateListener(OnWebrtcServerInfoUpdateListener onWebrtcServerInfoUpdateListener) {
        this.onWebrtcServerInfoUpdateListener = onWebrtcServerInfoUpdateListener;
    }

    public interface OnWebrtcServerInfoUpdateListener {
        void onUpdate(String ip);
    }

    public void setOnUsbDiskEventListener(Consumer<Boolean> onUsbDiskEventListener) {
        this.onUsbDiskEventListener = onUsbDiskEventListener;
    }

    public void setOnCameraEventListener(OnCameraEventListener onCameraEventListener) {
        this.onCameraEventListener = onCameraEventListener;
    }

    public interface OnCameraEventListener {
        void onCameraUpdate(boolean isUsbCamera);
    }

    public void onRemoteCameraCheckResp(boolean isExist) {
        sendConnectChange(isExist);
    }

    public synchronized void checkConnect() {
        onRemoteCameraCheckResp(isExistRemoteCamera());
    }

    private void sendConnectChange(Boolean isConnected) {
        Intent intent = new Intent(ZEE_SETTINGS_CAMERA_STATE);
        intent.putExtra("message", isConnected);
        sendBroadcast(intent);
    }

    private void sendOtherChange(int message) {
        Intent intent = new Intent(ZEE_SETTINGS_CAMERA_STATE);
        intent.putExtra("type", "Mobile");
        intent.putExtra("action", message);
        sendBroadcast(intent);
    }
}
