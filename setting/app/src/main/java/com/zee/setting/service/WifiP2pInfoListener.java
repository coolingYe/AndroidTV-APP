package com.zee.setting.service;

import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;

public interface WifiP2pInfoListener {
    void onPeersAvailable(WifiP2pDeviceList wifiP2pDeviceList);
    void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo);
    void onDiscoveryStateChanged(int state);
    void onUpdateThisDevice(WifiP2pDevice device);
}
