package com.zee.setting.receive;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Build;

import androidx.annotation.RequiresApi;

public class WifiBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "wwwhhh";
    private IntentFilter filter;
    private Context context;
    private WifiStateChangeListener wifiStateChangeListener;
    private WifiManager wifiManager;

    public WifiBroadcastReceiver(Context context, WifiManager wifiManager) {
        this.context = context;
        this.wifiManager = wifiManager;
        filter = new IntentFilter();
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);//wifi开关变化广播
        filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);//热点扫描结果通知广播
        filter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);//—热点连接结果通知广播
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);  //—网络状态变化广播（与上一广播协同完成连接过程通知）
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        context.registerReceiver(this, filter);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onReceive(Context context, Intent intent) {
        if (null != wifiStateChangeListener) {
            wifiStateChangeListener.onWifiChange(intent);
        }

    }

    public void unRegister() {
        context.unregisterReceiver(this);
    }


    public void setWifiStateChangeListener(WifiStateChangeListener wifiStateChangeListener) {
        this.wifiStateChangeListener = wifiStateChangeListener;
    }

    public interface WifiStateChangeListener {


        void onWifiChange(Intent intent);
    }

}
