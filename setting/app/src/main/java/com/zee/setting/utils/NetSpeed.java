package com.zee.setting.utils;

import android.content.Context;
import android.net.TrafficStats;

public class NetSpeed {

    private static long lastTotalRxBytes = 0;
    private static long lastTotalTxBytes = 0;
    private static long lastTimeStamp = 0;
    private static final String TAG = "ssshhh";

    public static String getNetSpeed(int uid) {
        long nowTotalRxBytes = getTotalRxBytes(uid);
        long nowTotalTxBytes = getTotalTxBytes(uid);
        long nowTimeStamp = System.currentTimeMillis();
      //  Log.d(TAG, "网络-x----速度-nowTotalTxBytes:" + nowTotalTxBytes+" 时间："+(nowTimeStamp - lastTimeStamp) +"差量："+(nowTotalTxBytes - lastTotalTxBytes)+" nowTotalTxBytes:"+nowTotalTxBytes+" lastTotalTxBytes:"+lastTotalTxBytes);
        long speedRx = ((nowTotalRxBytes - lastTotalRxBytes) * 1000 / (nowTimeStamp - lastTimeStamp));//毫秒转换
        long speedTx = ((nowTotalTxBytes - lastTotalTxBytes) * 1000 / (nowTimeStamp - lastTimeStamp));//毫秒转换
        lastTimeStamp = nowTimeStamp;
        lastTotalRxBytes = nowTotalRxBytes;
        lastTotalTxBytes = nowTotalTxBytes;
       // Log.d(TAG, "网络-x-速度下行:" + (speedRx + " kB/s") + " 上行:" + speedTx + " kB/s");
        float speed=((float) speedRx/1024)*8;
        return speed + "";
       // return speedRx + " kB/s";
    }


    /**
     * 下行 700kB/s 直播临界
     * 实际用到的流量
     * @param uid getApplicationInfo().uid 当前线程
     * @return
     */
    public static long getTotalRxBytes(int uid) {
        return TrafficStats.getUidRxBytes(uid) == TrafficStats.UNSUPPORTED ? 0 : (TrafficStats.getTotalRxBytes() / 1024);//转为KB
    }

    /**
     * 上行实际用到的流量
     * @param uid getApplicationInfo().uid 当前线程
     * @return
     */
    public static long getTotalTxBytes(int uid) {
        return TrafficStats.getUidRxBytes(uid) == TrafficStats.UNSUPPORTED ? 0 : (TrafficStats.getTotalTxBytes() / 1024);//转为KB
    }

    /**
     * 得到网络速度
     * @param context
     * @return
     */
    private static long lastTotalRxBytes1 = 0;
    private static long lastTimeStamp1 = 0;
    public static String getNetSpeed(Context context) {
        String netSpeed = "0 kb/s";
        long nowTotalRxBytes = TrafficStats.getUidRxBytes(context.getApplicationInfo().uid)==TrafficStats.UNSUPPORTED ? 0 :(TrafficStats.getTotalRxBytes()/1024);//转为KB;
        long nowTimeStamp = System.currentTimeMillis();
        long speed = ((nowTotalRxBytes - lastTotalRxBytes1) * 1000 / (nowTimeStamp - lastTimeStamp1));//毫秒转换

        lastTimeStamp1 = nowTimeStamp;
        lastTotalRxBytes1 = nowTotalRxBytes;
        netSpeed  = String.valueOf(speed) + " kb/s";
        return  netSpeed;
    }

}
