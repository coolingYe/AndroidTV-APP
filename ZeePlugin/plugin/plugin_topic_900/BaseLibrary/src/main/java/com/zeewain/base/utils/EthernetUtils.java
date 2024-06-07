package com.zeewain.base.utils;

import java.io.FileInputStream;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;

public class EthernetUtils {
    /**
     * 获取本地MAC地址
     */
    public static String getLocalMacAddress() {
        String mac = "";
        try {
            String path = "sys/class/net/eth0/address";
            FileInputStream fis_name = new FileInputStream(path);
            byte[] buffer_name = new byte[1024 * 8];
            int byteCount_name = fis_name.read(buffer_name);
            if (byteCount_name > 0) {
                mac = new String(buffer_name, 0, byteCount_name, "utf-8");
            }

            if (mac.length() == 0) {
                path = "sys/class/net/eth0/wlan0";
                FileInputStream fis = new FileInputStream(path);
                byte[] buffer = new byte[1024 * 8];
                int byteCount = fis.read(buffer);
                if (byteCount > 0) {
                    mac = new String(buffer, 0, byteCount, "utf-8");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("mac=====" + mac);
        return mac.trim();
    }

    public static String getEthernetMac() {
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (!nif.getName().equalsIgnoreCase("eth0")) continue;

                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes == null) {
                    return "02:00:00:00:00:00";
                }

                StringBuilder res1 = new StringBuilder();
                for (byte b : macBytes) {
                    res1.append(String.format("%02X:", b));
                }

                if (res1.length() > 0) {
                    res1.deleteCharAt(res1.length() - 1);
                }
                return res1.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "02:00:00:00:00:00";
    }

    public static String getWifiMac() {
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (!nif.getName().equalsIgnoreCase("wlan0")) continue;

                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes == null) {
                    return "02:00:00:00:00:00";
                }

                StringBuilder res1 = new StringBuilder();
                for (byte b : macBytes) {
                    res1.append(String.format("%02X:", b));
                }

                if (res1.length() > 0) {
                    res1.deleteCharAt(res1.length() - 1);
                }
                return res1.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "02:00:00:00:00:00";

    }


    /**
     * 获取无线MAC地址
     */
    public static String getWifeMacAddress() {
        try {
            // 把当前机器上访问网络的接口存入 List集合中
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (!"wlan0".equalsIgnoreCase(nif.getName())) {
                    continue;
                }
                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes == null || macBytes.length == 0) {
                    continue;
                }
                StringBuilder result = new StringBuilder();
                for (byte b : macBytes) {
                    //每隔两个字符加一个:
                    result.append(String.format("%02X:", b));
                }
                if (result.length() > 0) {
                    //删除最后一个:
                    result.deleteCharAt(result.length() - 1);
                }
                return result.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Unknown";
    }

}
