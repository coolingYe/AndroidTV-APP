package com.zeewain.base.utils;

import static android.net.ConnectivityManager.TYPE_ETHERNET;
import static android.net.ConnectivityManager.TYPE_MOBILE;
import static android.net.ConnectivityManager.TYPE_WIFI;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

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




}
