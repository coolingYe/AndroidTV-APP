package com.zee.setting.receive;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;

import com.zee.setting.utils.NetworkUtil;

public class NetworkChangeReceiver extends BroadcastReceiver {
    private Context context;
    private NetworkChangeListener networkChangeListener;


    public NetworkChangeReceiver(Context context) {
        this.context = context;
        registerReceiver(context);
    }


    @Override
    public void onReceive(Context context, Intent intent) {
        int netWorkType = NetworkUtil.getNetWorkType(context);
        if (networkChangeListener!=null){
            networkChangeListener.onNetworkChange(netWorkType);
        }

    }


    private void registerReceiver(Context context) {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        context.registerReceiver(this, intentFilter);
    }

    public void unRegister() {
        context.unregisterReceiver(this);
    }

    public void setNetworkChangeListener(NetworkChangeListener networkChangeListener) {
        this.networkChangeListener = networkChangeListener;
    }




    public interface NetworkChangeListener {
        void onNetworkChange(int status);
    }
}
