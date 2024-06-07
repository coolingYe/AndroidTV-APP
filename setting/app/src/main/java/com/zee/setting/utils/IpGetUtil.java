package com.zee.setting.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.EthernetManager;
import android.net.IpConfiguration;
import android.net.LinkAddress;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkUtils;
import android.net.StaticIpConfiguration;
import android.util.Log;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.regex.Pattern;

public class IpGetUtil {
    private static final String TAG = "IpGetUtil";

    /**
     * Ipv4 address check.
     */
    private static final Pattern IPV4_PATTERN = Pattern.compile(
            "^(" + "([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}" +
                    "([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$");

    /**
     * Check if valid IPV4 address.
     *
     * @param input the address string to check for validity.
     *
     * @return True if the input parameter is a valid IPv4 address.
     */
    public static boolean isIPv4Address(String input) {
        return IPV4_PATTERN.matcher(input).matches();
    }

    /**
     * Get local Ip address.
     */
    public static InetAddress getLocalIPAddress() {
        Enumeration<NetworkInterface> enumeration = null;
        try {
            enumeration = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
            e.printStackTrace();
        }
        if (enumeration != null) {
            while (enumeration.hasMoreElements()) {
                NetworkInterface nif = enumeration.nextElement();
                Enumeration<InetAddress> inetAddresses = nif.getInetAddresses();
                if (inetAddresses != null) {
                    while (inetAddresses.hasMoreElements()) {
                        InetAddress inetAddress = inetAddresses.nextElement();
                        if (!inetAddress.isLoopbackAddress() && isIPv4Address(inetAddress.getHostAddress())) {
                            return inetAddress;
                        }
                    }
                }
            }
        }
        return null;
    }

    //获取以太网的IP地址
    public static String getIpAddress(Context context) {
        try {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            Network network = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                network = mConnectivityManager.getActiveNetwork();
            }
            LinkProperties linkProperties = mConnectivityManager.getLinkProperties(network);
            for (LinkAddress linkAddress : linkProperties.getLinkAddresses()) {
                InetAddress address = linkAddress.getAddress();
                if (address instanceof Inet4Address) {
                    return address.getHostAddress();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "getIpAddress error" + e.getMessage(), e);
        }
        // IPv6 address will not be shown like WifiInfo internally does.
        return "";
    }



    /*
     * convert subMask string to prefix length
     */
    public static int maskStr2InetMask(String maskStr) {
        StringBuffer sb;
        String str;
        int inetmask = 0;
        int count = 0;
        /*
         * check the subMask format
         */
        Pattern pattern = Pattern.compile("(^((\\d|[01]?\\d\\d|2[0-4]\\d|25[0-5])\\.){3}(\\d|[01]?\\d\\d|2[0-4]\\d|25[0-5])$)|^(\\d|[1-2]\\d|3[0-2])$");
        if (pattern.matcher(maskStr).matches() == false) {
            Log.e(TAG, "subMask is error");
            return 0;
        }

        String[] ipSegment = maskStr.split("\\.");
        for (int n = 0; n < ipSegment.length; n++) {
            sb = new StringBuffer(Integer.toBinaryString(Integer.parseInt(ipSegment[n])));
            str = sb.reverse().toString();
            count = 0;
            for (int i = 0; i < str.length(); i++) {
                i = str.indexOf("1", i);
                if (i == -1)
                    break;
                count++;
            }
            inetmask += count;
        }
        return inetmask;
    }

    public static InetAddress getIPv4Address(String text) {
        try {
            return NetworkUtils.numericToInetAddress(text);
        } catch (IllegalArgumentException | ClassCastException e) {
            return null;
        }
    }


    public static boolean setEthernetIP(Context context, String mode, String ipAddress, String netmask,
                                        String gateway, String dns1, String dns2) {
        EthernetManager mEthManager = (EthernetManager)  context.getSystemService("ethernet");
        StaticIpConfiguration mStaticIpConfiguration = new StaticIpConfiguration();
        /*
         * get ip address, netmask,dns ,gw etc.
         */
        InetAddress inetAddr =  getIPv4Address(ipAddress);
        int prefixLength = maskStr2InetMask(netmask);
        InetAddress gatewayAddr = getIPv4Address(gateway);
        InetAddress dnsAddr = getIPv4Address(dns1);

        if (inetAddr.getAddress().toString().isEmpty() || prefixLength ==0 || gatewayAddr.toString().isEmpty()
                || dnsAddr.toString().isEmpty()) {
            return false;
        }

        Class<?> clazz = null;
        try {
            clazz = Class.forName("android.net.LinkAddress");
        } catch (Exception e) {
            // TODO: handle exception
        }

        Class[] cl = new Class[]{InetAddress.class, int.class};
        Constructor cons = null;

        //取得所有构造函数
        try {
            cons = clazz.getConstructor(cl);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }

        //给传入参数赋初值
        Object[] x = {inetAddr, prefixLength};

        String dnsStr2 = dns2;
        //mStaticIpConfiguration.ipAddress = new LinkAddress(inetAddr, prefixLength);
        try {
            mStaticIpConfiguration.ipAddress = (LinkAddress) cons.newInstance(x);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

        mStaticIpConfiguration.gateway=gatewayAddr;
        mStaticIpConfiguration.dnsServers.add(dnsAddr);

        if (!dnsStr2.isEmpty()) {
            mStaticIpConfiguration.dnsServers.add(getIPv4Address(dnsStr2));
        }

        Log.d("2312321", "chanson mStaticIpConfiguration  ====" + mStaticIpConfiguration);

        IpConfiguration mIpConfiguration = new IpConfiguration(IpConfiguration.IpAssignment.STATIC, IpConfiguration.ProxySettings.NONE, mStaticIpConfiguration, null);
        String[] ifaces = mEthManager.getAvailableInterfaces();
        if (ifaces.length <= 0) {
            Log.e(TAG, " setEthernetIP failed ifaces.length <= 0");
            return false ;
        }
        String mInterfaceName = ifaces[0];
        mEthManager.setConfiguration(mInterfaceName,mIpConfiguration);
        return  true;
    }



}
