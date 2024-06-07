package com.zee.setting.utils;

import static android.content.Context.WIFI_SERVICE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.IpConfiguration;
import android.net.LinkAddress;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;
import android.util.Log;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WifiUtil {

    @SuppressLint({"MissingPermission", "HardwareIds"})
    public static void getDetailsWifiInfo(Context context) {
        StringBuilder sInfo = new StringBuilder();
        WifiManager mWifiManager = (WifiManager) context.getApplicationContext().getSystemService(WIFI_SERVICE);
        WifiInfo mWifiInfo = mWifiManager.getConnectionInfo();
        int Ip = mWifiInfo.getIpAddress();
        String strIp = "" + (Ip & 0xFF) + "." + ((Ip >> 8) & 0xFF) + "." + ((Ip >> 16) & 0xFF) + "." + ((Ip >> 24) & 0xFF);
        sInfo.append("\n--BSSID : ").append(mWifiInfo.getBSSID());
        sInfo.append("\n--SSID : ").append(mWifiInfo.getSSID());
        sInfo.append("\n--nIpAddress : ").append(strIp);
        sInfo.append("\n--MacAddress : ").append(mWifiInfo.getMacAddress());
        sInfo.append("\n--NetworkId : ").append(mWifiInfo.getNetworkId());
        sInfo.append("\n--LinkSpeed : ").append(mWifiInfo.getLinkSpeed()).append("Mbps");
        sInfo.append("\n--Rssi : ").append(mWifiInfo.getRssi());
        sInfo.append("\n--SupplicantState : ").append(mWifiInfo.getSupplicantState()).append(mWifiInfo);
        sInfo.append("\n\n\n\n");
        Log.d("getDetailsWifiInfo", sInfo.toString());
    }

    public static List<String> getAroundWifiDeviceInfo(Context context) {
        WifiManager mWifiManager = (WifiManager) context.getApplicationContext().getSystemService(WIFI_SERVICE);
        WifiInfo mWifiInfo = mWifiManager.getConnectionInfo();
        List<ScanResult> scanResults = mWifiManager.getScanResults();//搜索到的设备列表
        List<ScanResult> newScanResultList = new ArrayList<>();
        for (ScanResult scanResult : scanResults) {
            int position = getItemPosition(newScanResultList, scanResult);
            if (position != -1) {
                if (newScanResultList.get(position).level < scanResult.level) {
                    newScanResultList.remove(position);
                    newScanResultList.add(position, scanResult);
                }
            } else {
                newScanResultList.add(scanResult);
            }
        }
        List<String> stringList = new ArrayList<>();
        for (int i = 0; i < newScanResultList.size(); i++) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("设备名(SSID) ->" + newScanResultList.get(i).SSID + "\n");
            stringBuilder.append("信号强度 ->" + newScanResultList.get(i).level + "\n");
            stringBuilder.append("BSSID ->" + newScanResultList.get(i).BSSID + "\n");
            stringBuilder.append("level ->" + newScanResultList.get(i).level + "\n");
            stringBuilder.append("采集时间戳 ->" + System.currentTimeMillis() + "\n");
            stringBuilder.append("rssi ->" + (mWifiInfo != null && (mWifiInfo.getSSID().replace("\"", "")).equals(newScanResultList.get(i).SSID) ? mWifiInfo.getRssi() : null) + "\n");
            //是否为连接信号(1连接，默认为null)
            stringBuilder.append("是否为连接信号 ->" + (mWifiInfo != null && (mWifiInfo.getSSID().replace("\"", "")).equals(newScanResultList.get(i).SSID) ? 1 : null) + "\n");
            stringBuilder.append("信道 - >" + getCurrentChannel(mWifiManager) + "\n");
            //1 为2.4g 2 为5g
            stringBuilder.append("频段 ->" + is24GOr5GHz(newScanResultList.get(i).frequency));
            stringList.add(stringBuilder.toString());
        }
        Log.d("getAroundWifiDeviceInfo", "");
        return stringList;
    }


    public static String is24GOr5GHz(int freq) {
        if (freq > 2400 && freq < 2500) {
            return "1";
        } else if (freq > 4900 && freq < 5900) {
            return "2";
        } else {
            return "无法判断";
        }
    }

    /**
     * 返回item在list中的坐标
     */
    private static int getItemPosition(List<ScanResult> list, ScanResult item) {
        for (int i = 0; i < list.size(); i++) {
            if (item.SSID.equals(list.get(i).SSID)) {
                return i;
            }
        }
        return -1;
    }

    public static int getCurrentChannel(WifiManager wifiManager) {
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();// 当前wifi连接信息
        List<ScanResult> scanResults = wifiManager.getScanResults();
        for (ScanResult result : scanResults) {
            if (result.BSSID.equalsIgnoreCase(wifiInfo.getBSSID())
                    && result.SSID.equalsIgnoreCase(wifiInfo.getSSID()
                    .substring(1, wifiInfo.getSSID().length() - 1))) {
                return getChannelByFrequency(result.frequency);
            }
        }
        return -1;
    }

    /**
     * 根据频率获得信道
     *
     * @param frequency
     * @return
     */
    public static int getChannelByFrequency(int frequency) {
        int channel = -1;
        switch (frequency) {
            case 2412:
                channel = 1;
                break;
            case 2417:
                channel = 2;
                break;
            case 2422:
                channel = 3;
                break;
            case 2427:
                channel = 4;
                break;
            case 2432:
                channel = 5;
                break;
            case 2437:
                channel = 6;
                break;
            case 2442:
                channel = 7;
                break;
            case 2447:
                channel = 8;
                break;
            case 2452:
                channel = 9;
                break;
            case 2457:
                channel = 10;
                break;
            case 2462:
                channel = 11;
                break;
            case 2467:
                channel = 12;
                break;
            case 2472:
                channel = 13;
                break;
            case 2484:
                channel = 14;
                break;
            case 5745:
                channel = 149;
                break;
            case 5765:
                channel = 153;
                break;
            case 5785:
                channel = 157;
                break;
            case 5805:
                channel = 161;
                break;
            case 5825:
                channel = 165;
                break;
        }
        return channel;
    }


    public static boolean addWifi(WifiManager wifiManager, String ssid, String pwd, int type) {
        boolean isSuccess = false;
        boolean flag = false;
        wifiManager.disconnect();
        //  boolean addSucess = addNetwork(CreateWifiInfo(ssid, pwd, 3));
        WifiConfiguration wifiConfiguration = createWifiConfiguration(wifiManager, ssid, pwd, type);
        int wcgID = wifiManager.addNetwork(wifiConfiguration);
        boolean addSucess = wifiManager.enableNetwork(wcgID, true);
        return addSucess;
    }

    public static WifiConfiguration createWifiConfiguration(WifiManager wifiManager, String SSID, String password, int type) {
        WifiConfiguration config = new WifiConfiguration();
        config.allowedAuthAlgorithms.clear();
        config.allowedGroupCiphers.clear();
        config.allowedKeyManagement.clear();
        config.allowedPairwiseCiphers.clear();
        config.allowedProtocols.clear();
        config.SSID = "\"" + SSID + "\"";

        WifiConfiguration tempConfig = isExist(wifiManager, SSID);
        if (tempConfig != null) {
            wifiManager.removeNetwork(tempConfig.networkId);
        }

        if (type == 1) {//NO PASSWORD
//            config.wepKeys[0] = "";
            config.hiddenSSID = true;
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
//            config.wepTxKeyIndex = 0;
        } else if (type == 2) {//WEP
            config.hiddenSSID = true;
            config.wepKeys[0] = "\"" + password + "\"";
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;
        } else if (type == 3) {//WPA
            config.preSharedKey = "\"" + password + "\"";
            config.hiddenSSID = true;
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            //config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            config.status = WifiConfiguration.Status.ENABLED;
        }
        return config;
    }

    public static WifiConfiguration isExist(WifiManager wifiManager, String SSID) {
        @SuppressLint("MissingPermission")
        List<WifiConfiguration> existingConfigs = wifiManager.getConfiguredNetworks();
        for (WifiConfiguration existingConfig : existingConfigs) {
            if (existingConfig.SSID.equals("\"" + SSID + "\"")) {
                return existingConfig;
            }
        }
        return null;
    }

    public static String intToIp(int paramInt) {
        return (paramInt & 0xFF) + "." +
                (0xFF & paramInt >> 8) + "." +
                (0xFF & paramInt >> 16) + "." +
                (0xFF & paramInt >> 24);
    }

    public static String getNetMask(Context context) {
        DhcpInfo dhcpInfo = ((WifiManager) context
                .getSystemService(WIFI_SERVICE)).getDhcpInfo();
        return formatIpAddress(dhcpInfo.netmask);
    }

    public static String formatIpAddress(int ipAdress) {

        return (ipAdress & 0xFF) + "." +
                ((ipAdress >> 8) & 0xFF) + "." +
                ((ipAdress >> 16) & 0xFF) + "." +
                (ipAdress >> 24 & 0xFF);
    }


    /**
     * 设置WiFi静态ip等配置
     *
     * @param context
     * @param ip
     * @param gateWay
     * @param dns1
     * @param dns2
     */
    public static boolean changeWifiConfiguration(Context context, String ssid, String ip, String netMask, String gateWay, String dns1, String dns2) {
        try {
            InetAddress inetAddr = InetAddress.getByName(ip);
            int prefixLength = submaskStr2PrefixLen(netMask);
            InetAddress gatewayAddr = InetAddress.getByName(gateWay);
            if (TextUtils.isEmpty(dns1)) {
                dns1 = "0.0.0.0";
            }
            InetAddress dns1Addr = InetAddress.getByName(dns1);
            if (TextUtils.isEmpty(dns2)) {
                dns2 = "0.0.0.0";
            }
            InetAddress dns2Addr = InetAddress.getByName(dns2);

            //取得所有构造函数
            Class[] cl = new Class[]{InetAddress.class, int.class};
            Class<?> clazz = Class.forName("android.net.LinkAddress");
            Constructor cons = clazz.getConstructor(cl);
            if (cons == null) {
                return false;
            }

            //给传入参数赋初值
            Object[] x = {inetAddr, prefixLength};

            //构造StaticIpConfiguration对象
            Class<?> staticIpConfigurationCls = Class.forName("android.net.StaticIpConfiguration");

            //实例化StaticIpConfiguration
            Object staticIpConfiguration = null;

            staticIpConfiguration = staticIpConfigurationCls.newInstance();
            Field ipAddress = staticIpConfigurationCls.getField("ipAddress");
            Field gateway = staticIpConfigurationCls.getField("gateway");
            Field dnsServers = staticIpConfigurationCls.getField("dnsServers");

            //设置ipAddress
            ipAddress.set(staticIpConfiguration, (LinkAddress) cons.newInstance(x));

            //设置网关
            gateway.set(staticIpConfiguration, gatewayAddr);

            //设置dns
            ArrayList<InetAddress> dnsList = (ArrayList<InetAddress>) dnsServers.get(staticIpConfiguration);
            dnsList.add(dns1Addr);
            // dnsList.add(dns2Addr);  //暂时不设置dns2

            WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(WIFI_SERVICE);
            WifiConfiguration wifiConfig = null;

            //得到连接的wifi网络
            WifiInfo connectionInfo = wifiManager.getConnectionInfo();
            if (!connectionInfo.getSSID().equals("\"" + ssid + "\"")) {
                return false;
            }

            @SuppressLint("MissingPermission")
            List<WifiConfiguration> configuredNetworks = wifiManager.getConfiguredNetworks();
            for (WifiConfiguration conf : configuredNetworks) {
                if (conf.networkId == connectionInfo.getNetworkId()) {
                    wifiConfig = conf;
                    break;
                }
            }

            @SuppressLint("PrivateApi")
            Class ipAssignmentCls = Class.forName("android.net.IpConfiguration$IpAssignment");
            Object ipAssignment = Enum.valueOf(ipAssignmentCls, "STATIC");
            Method setIpAssignmentMethod = wifiConfig.getClass().getDeclaredMethod("setIpAssignment", ipAssignmentCls);
            setIpAssignmentMethod.invoke(wifiConfig, ipAssignment);

            if (null == wifiConfig.getClass()) {
                return false;
            }

            Method setStaticIpConfigurationMethod = wifiConfig.getClass().getDeclaredMethod("setStaticIpConfiguration", staticIpConfiguration.getClass());

            //设置静态IP，将StaticIpConfiguration设置给WifiConfiguration
            setStaticIpConfigurationMethod.invoke(wifiConfig, staticIpConfiguration);

            //WifiConfiguration重新添加到WifiManager
          /*  int netId = wifiManager.addNetwork(wifiConfig);
            wifiManager.disableNetwork(netId);
            boolean flag = wifiManager.enableNetwork(netId, true);
            return flag;*/
            int networkId = wifiManager.updateNetwork(wifiConfig);
           // 连接WiFi网络
            wifiManager.disconnect();
            boolean flag = wifiManager.enableNetwork(networkId, true);
            wifiManager.reconnect();
            return flag;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 将string类型的子网掩码转为prefixLength,代码如下
     */
    public static int submaskStr2PrefixLen(String maskStr) throws Exception {
        StringBuffer sb;
        String str;
        int inetmask = 0;
        int count = 0;

        //检查子网掩码的格式
        Pattern pattern = Pattern.compile(
                "(^((\\d|[01]?\\d\\d|2[0-4]\\d|25[0-5])\\.){3}(\\d|[01]?\\d\\d|2[0-4]\\d|25[0-5])$)|^(\\d|[1-2]\\d|3[0-2])$");
        if (null != pattern) {
            Matcher matcher = pattern.matcher(maskStr);
            if (null != matcher) {
                boolean matches = matcher.matches();
                if (matches == false) {
                    return 0;
                }
            }
        }

        String[] ipSegment = maskStr.split("\\.");
        for (int n = 0; n < ipSegment.length; n++) {
            sb = new StringBuffer(Integer.toBinaryString(Integer.parseInt(ipSegment[n])));
            str = sb.reverse().toString();
            count = 0;
            for (int i = 0; i < str.length(); i++) {
                i = str.indexOf("1", i);
                if (i == -1) {
                    break;
                }
                count++;
            }
            inetmask += count;
        }
        return inetmask;
    }


    public static void setWifiDHCP(Context context) {
        try {
            WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(WIFI_SERVICE);
            WifiConfiguration wifiConfig = null;
            WifiInfo connectionInfo = wifiManager.getConnectionInfo();  //得到连接的wifi网络

            List<WifiConfiguration> configuredNetworks = wifiManager.getConfiguredNetworks();
            for (WifiConfiguration conf : configuredNetworks) {
                if (conf.networkId == connectionInfo.getNetworkId()) {
                    wifiConfig = conf;
                    break;
                }
            }

            Class ipAssignmentCls = Class.forName("android.net.IpConfiguration$IpAssignment");
            Method setIpAssignmentMethod = wifiConfig.getClass().getDeclaredMethod("setIpAssignment", ipAssignmentCls);
            setIpAssignmentMethod.invoke(wifiConfig, IpConfiguration.IpAssignment.DHCP);
         /*   int netId = wifiManager.addNetwork(wifiConfig);
            wifiManager.disableNetwork(netId);
            wifiManager.enableNetwork(netId, true);*/
            int networkId = wifiManager.updateNetwork(wifiConfig);
            // 连接WiFi网络
            wifiManager.disconnect();
            wifiManager.enableNetwork(networkId, true);
            wifiManager.reconnect();



        } catch (IllegalAccessException | InvocationTargetException | ClassNotFoundException | NoSuchMethodException e) {
            e.printStackTrace();
        }

    }


    /**
     * 解决DhcpInfo获取WiFi子网掩码为0的问题
     *
     * @return
     */
    public static String getWifiMask() {
        try {
            Enumeration<NetworkInterface> networkInterfaceEnumeration = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaceEnumeration.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaceEnumeration.nextElement();
                if (!networkInterface.isUp()) {
                    continue;
                }
                for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
                    if (interfaceAddress.getAddress() instanceof Inet4Address) {
                        if (!"127.0.0.1".equals(interfaceAddress.getAddress().getHostAddress())) {
                            return calcMaskByPrefixLength(interfaceAddress.getNetworkPrefixLength());
                        }
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String calcMaskByPrefixLength(int length) {
        int mask = 0xffffffff << (32 - length);
        int partsNum = 4;
        int bitsOfPart = 8;
        int[] maskParts = new int[partsNum];
        int selector = 0x000000ff;

        for (int i = 0; i < maskParts.length; i++) {
            int pos = maskParts.length - 1 - i;
            maskParts[pos] = (mask >> (i * bitsOfPart)) & selector;
        }

        String result = "";
        result = result + maskParts[0];
        for (int i = 1; i < maskParts.length; i++) {
            result = result + "." + maskParts[i];
        }
        return result;
    }


    public static String getWifiSetting(Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        DhcpInfo dhcpInfo = wifiManager.getDhcpInfo();
//        Log.i("ssshhh","leaseDuration="+dhcpInfo.leaseDuration);
//        Log.i("ssshhh","serverAddress="+dhcpInfo.serverAddress);
        if (dhcpInfo.leaseDuration == 0) {//静态IP配置方式
            return "StaticIP";
        } else {                         //动态IP配置方式
            return "DHCP";
        }

    }

    // 判断是否是用wifi
    public static boolean isWiFiActive(Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        return wifiManager.isWifiEnabled();
    }


}
