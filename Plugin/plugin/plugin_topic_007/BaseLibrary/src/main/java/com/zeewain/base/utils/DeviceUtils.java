package com.zeewain.base.utils;

import android.content.Context;
import android.os.Build;
import android.provider.Settings;

import java.lang.reflect.Method;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class DeviceUtils {
    /**
     * 设备名称
     *
     * @return 设备名称
     */
    public static String getDeviceName(Context context) {
        return Settings.Global.getString(context.getContentResolver(), Settings.Global.DEVICE_NAME);
      //  return android.os.Build.DEVICE;
       // return Build.PRODUCT;
    }

    /**
     * 设备型号
     *
     * @return 设备型号
     */
    public static String getModelName() {
        return Build.MODEL;
    }

    /**
     * 型号识别码
     *
     * @return
     */
    public static String getModelIdentificationCode(Context context) {
        return Settings.System.getString(context.getContentResolver(), Settings.System.ANDROID_ID);
    }

    /**
     * 获取当前手机系统版本号
     *
     * @return 系统版本号
     */
    public static String getSystemVersion() {
        return Build.VERSION.RELEASE;
    }

    /**
     * 获取厂商
     *
     * @return 厂商
     */
    public static String getBrand() {
        return Build.BRAND;
    }

    /**
     * 获取设备制造商
     *
     * @return 制造商
     */
    public static String getManufacturer() {
        return Build.MANUFACTURER;
    }

    /**
     * SDK 版本
     * @return
     */
    public static String getSDKVersion() {
       String SDKVersion = Build.VERSION.SDK ;
        return  SDKVersion;
    }

    /**
     * 安卓版本
     * @return
     */
    public static String getAndroidVersion() {
        String androidVersion =Build.VERSION.RELEASE;
        return  androidVersion;
    }


    /**
     * 获取当前手机系统语言。
     *
     * @return 返回当前系统语言。例如：当前设置的是“中文-中国”，则返回“zh-CN”
     */
    public static String getSystemLanguage() {
        return Locale.getDefault().getLanguage();
    }

    /**
     * 获取当前系统上的语言列表(Locale列表)
     *
     * @return  语言列表
     */
    public static Locale[] getSystemLanguageList() {
        return Locale.getAvailableLocales();
    }

    /**
     * 获取设备序列号
     *
     * @return
     */
    public static String getSerialNum() {
        String serial = "";
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {//9.0+
                serial = Build.getSerial();
            } else if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N) {//8.0+
                serial = Build.SERIAL;
            } else {//8.0-
                Class<?> c = Class.forName("android.os.SystemProperties");
                Method get = c.getMethod("get", String.class);
                serial = (String) get.invoke(c, "ro.serialno");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return serial;
    }


    /**
     * 获取设备序列号
     *
     * @return
     */
    public static String getSN() {
        String sn = "null";
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
//            Android 10不再支持获取SN，需要系统签名
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                sn = Build.getSerial();
            } else {
                sn = Build.SERIAL;
            }
        }
        return sn;
    }



    /**
     * 获取MAC地址
     */
    public static String getMacAddress() {
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



}
