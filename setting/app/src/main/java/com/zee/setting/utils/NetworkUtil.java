package com.zee.setting.utils;

import static android.net.ConnectivityManager.TYPE_ETHERNET;
import static android.net.ConnectivityManager.TYPE_MOBILE;
import static android.net.ConnectivityManager.TYPE_WIFI;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pDevice;
import android.util.Log;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class NetworkUtil {
    public static final int NETWORK_NO = -1;
    public static final int NETWORK_WIFI = 0;
    public static final int NETWORK_ETHERNET = 1;
    public static final int NETWORK_MOBILE = 2;

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getApplicationContext().getSystemService(
                Context.CONNECTIVITY_SERVICE);
        if (null == manager)
            return false;
        NetworkInfo info = manager.getActiveNetworkInfo();
        if (null == info || !info.isAvailable())
            return false;
        return true;
    }



    /**
     * 取得ConnectivityManager对象
     *
     * @param context
     * @return
     */
    public static ConnectivityManager getConnectivityManager(Context context) {
        return (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    }


    /**
     * 取得网络连接类型对象
     *
     * @param context
     * @return
     */

    public static int getNetWorkType(Context context){
        //检查用户的网络情况
        ConnectivityManager mConnectivityManager =getConnectivityManager(context);
        //返回当前可用网络信息
        NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
        if(mNetworkInfo!=null){
            // TelephonyManager telephonyManager = (TelephonyManager)MenuActivity.this.getSystemService(Context.TELEPHONY_SERVICE);
            if (mNetworkInfo.getType() == TYPE_ETHERNET){
                  return NETWORK_ETHERNET;
            }else if (mNetworkInfo.getType() == TYPE_WIFI){
                  return NETWORK_WIFI;
            }else if (mNetworkInfo.getType() == TYPE_MOBILE){
                return NETWORK_MOBILE;
            }
        }

        return  NETWORK_NO;

    }

    public static boolean isWifiConnected(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getApplicationContext().getSystemService(
                Context.CONNECTIVITY_SERVICE);

        if (null == manager)
            return false;

        NetworkInfo wifiNetworkInfo = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (wifiNetworkInfo != null && wifiNetworkInfo.isConnected()) {
            return true;
        }
        return false;
    }

    public static String getIpAddress(Context context) {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifiManager.isWifiEnabled()) {
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            if(wifiInfo != null) {
                int ipAddress = wifiInfo.getIpAddress();
                if(ipAddress != 0) {
                    return intToIp(ipAddress);
                }
            }
        }

        return null;
    }

    private static String intToIp(int ipAddress) {
        return (ipAddress & 0xFF) + "." +
                ((ipAddress >> 8) & 0xFF) + "." +
                ((ipAddress >> 16) & 0xFF) + "." +
                (ipAddress >> 24 & 0xFF);
    }

    public static String getLocalIpAddress() {
        String strIP=null;
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                        strIP= inetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (SocketException ex) {
            ex.printStackTrace();
            Log.e("NetworkUtil", ex.toString());
        }
        return strIP;
    }

    public static String getWiFiP2pDeviceStatus(int wifiP2pDeviceStatus) {
        switch (wifiP2pDeviceStatus) {
            case WifiP2pDevice.AVAILABLE:
                return "Available";
            case WifiP2pDevice.INVITED:
                return "Invited";
            case WifiP2pDevice.CONNECTED:
                return "Connected";
            case WifiP2pDevice.FAILED:
                return "Failed";
            case WifiP2pDevice.UNAVAILABLE:
                return "Unavailable";
            default:
                return "Unknown";

        }
    }

}
