package com.zwn.launcher.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.SystemClock;

public class SntpTimeUtil {
    public static long getCurrentTimeFromSntp(Context context){
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);;
        if (connectivityManager == null) {
            return 0;
        }
        final Network network = connectivityManager.getActiveNetwork();
        final NetworkInfo ni = connectivityManager.getNetworkInfo(network);
        if (ni == null || !ni.isConnected()) {
            return 0;
        }

        final SntpClient client = new SntpClient();
        final int timeoutMillis = 10000;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (client.requestTime("ntp.aliyun.com", timeoutMillis, network)) {
                return client.getNtpTime() + SystemClock.elapsedRealtime() - client.getNtpTimeReference();
            } else if (client.requestTime("ntp.tencent.com", timeoutMillis, network)) {
                return client.getNtpTime() + SystemClock.elapsedRealtime() - client.getNtpTimeReference();
            }
        }
        return 0;
    }
}
