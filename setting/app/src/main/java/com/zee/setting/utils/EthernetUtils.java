package com.zee.setting.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.EthernetManager;
import android.net.IpConfiguration;
import android.net.LinkAddress;
import android.net.StaticIpConfiguration;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class EthernetUtils {
    private static final String defaultIP = "0.0.0.0";
    private static final String command = "ifconfig";

    public static String getIpAddressForInterfaces() {

        String interfaceName = "eth0";

        try {
            Enumeration<NetworkInterface> enNetworkInterface = NetworkInterface.getNetworkInterfaces(); //获取本机所有的网络接口
            while (enNetworkInterface.hasMoreElements()) {  //判断 Enumeration 对象中是否还有数据
                NetworkInterface networkInterface = enNetworkInterface.nextElement();   //获取 Enumeration 对象中的下一个数据
                if (!networkInterface.isUp()) { // 判断网口是否在使用
                    continue;
                }
                if (!interfaceName.equals(networkInterface.getDisplayName())) { // 网口名称是否和需要的相同
                    continue;
                }
                Log.i("ppphhh", "getDisplayName=" + networkInterface.getDisplayName());
                Enumeration<InetAddress> enInetAddress = networkInterface.getInetAddresses();   //getInetAddresses 方法返回绑定到该网卡的所有的 IP 地址。
                while (enInetAddress.hasMoreElements()) {
                    InetAddress inetAddress = enInetAddress.nextElement();
                    if (inetAddress instanceof Inet4Address) {  //判断是否未ipv4
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "0.0.0.0";
    }

    public static String getGateWay() {
        String[] arr;
        try {
            Process process = Runtime.getRuntime().exec("ip route list table 0");
            BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String string = in.readLine();
            arr = string.split("\\s+");
            return arr[2];
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "0.0.0.0";
    }


    /**
     * 获取掩码
     *
     * @param name
     * @return
     */
    public static String getLocalMask(String name) {
        Process cmdProcess = null;
        BufferedReader reader = null;
        String dnsIP = "";
        try {
            cmdProcess = Runtime.getRuntime().exec("getprop dhcp." + name + ".mask");
            reader = new BufferedReader(new InputStreamReader(cmdProcess.getInputStream()));
            dnsIP = reader.readLine();
            return dnsIP;
        } catch (IOException e) {
            return null;
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
            }
            cmdProcess.destroy();
        }
    }


    /*获取子网掩码*/
    public static String getIpAddressMaskForInterfaces() {

        String interfaceName = "eth0";

        try {
            //获取本机所有的网络接口
            Enumeration<NetworkInterface> networkInterfaceEnumeration = NetworkInterface.getNetworkInterfaces();
            //判断 Enumeration 对象中是否还有数据
            while (networkInterfaceEnumeration.hasMoreElements()) {

                //获取 Enumeration 对象中的下一个数据
                NetworkInterface networkInterface = networkInterfaceEnumeration.nextElement();
                if (!networkInterface.isUp() && !interfaceName.equals(networkInterface.getDisplayName())) {

                    //判断网口是否在使用，判断是否时我们获取的网口
                    continue;
                }


                for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {

                    if (interfaceAddress.getAddress() instanceof Inet4Address) {
                        //仅仅处理ipv4
                        //获取掩码位数，通过 calcMaskByPrefixLength 转换为字符串
                        return calcMaskByPrefixLength(interfaceAddress.getNetworkPrefixLength());


                    }
                }

            }
        } catch (SocketException e) {

            e.printStackTrace();
        }

        return "0.0.0.0";
    }

    /*通过子网掩码的位数计算子网掩码*/
    public static String calcMaskByPrefixLength(int length) {

        int mask = 0xffffffff << (32 - length);
        int partsNum = 4;
        int bitsOfPart = 8;
        int maskParts[] = new int[partsNum];
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


    public static long calcMaskByPrefixLength2(int length) {
        int mask = -1 << (32 - length);
        int partsNum = 4;
        int bitsOfPart = 8;
        int maskParts[] = new int[partsNum];
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
        System.out.println(result);
        return (maskParts[0] << 24) + (maskParts[1] << 16) + (maskParts[2] << 8) + (maskParts[3]);
    }



    public static String getWlan0Mask() {
        String TAG = "lanky";
        String Mask = defaultIP;
        try {
            Process process = Runtime.getRuntime().exec(new String[]{command});
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            do {
                String line = bufferedReader.readLine();
                if (line == null) {
                    Log.d(TAG, "test: line is null");
                    break;
                }
                if (line.startsWith("wlan0     ")) {
                    Mask = defaultIP;
                }
                Mask = getLineMask(line).equals("") ? Mask : getLineMask(line);
                if (line.startsWith("p2p0      ")) {
                    break;
                }


            } while (true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Mask;
    }

    public static String getEth0Mask() {
        String TAG = "etho";
        String Mask = defaultIP;
        try {
            Process process = Runtime.getRuntime().exec(new String[]{command});
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            do {
                String line = bufferedReader.readLine();
                if (line == null) {
                    Log.d(TAG, "test: line is null");
                    break;
                }
                if (line.startsWith("eth0      ")) {
                    Mask = defaultIP;
                }
                Mask = getLineMask(line).equals("") ? Mask : getLineMask(line);
                if (line.startsWith("lo        ")) {
                    break;
                }
            } while (true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Mask;
    }

    private static String getLineMask(String line) {
        String TAG = "mask";
        String IP = "";
        if (line.trim().matches("inet addr:(\\d{1,3}\\.){3}\\d{1,3}( ){2}" +
                "(Bcast:(\\d{1,3}\\.){3}\\d{1,3}( ){2}){0,1}" +
                "Mask:(\\d{1,3}\\.){3}\\d{1,3}")) {
            String[] props = line.trim().split("( ){2}");
            for (String prop : props) {
                if (prop.length() == 0) {
                    continue;
                }

                String[] kv = prop.split(":");
                if (kv[0].startsWith("inet addr")) {
                    Log.d(TAG, "----ipAddr: " + kv[1]);
                } else if (kv[0].startsWith("Bcast")) {
                    Log.d(TAG, "----Bcast: " + kv[1]);
                } else if (kv[0].startsWith("Mask")) {
                    Log.d(TAG, "----mask: " + kv[1]);
                    IP = kv[1];
                }
            }
        }
        return IP;
    }


    public static void  getEthernetDhcpInfo(Context context){
        EthernetManager mEthManager = (EthernetManager) context.getSystemService("ethernet");
        String[] availableInterfaces = mEthManager.getAvailableInterfaces();
        String name="";
        for (int i=0;i<availableInterfaces.length;i++){
            name=availableInterfaces[i];
        }
        IpConfiguration ipConfiguration = mEthManager.getConfiguration(name);
        StaticIpConfiguration staticIpConfiguration = ipConfiguration.getStaticIpConfiguration();
        LinkAddress ipAddress = staticIpConfiguration.ipAddress;
        String ipv4=ipAddress.getAddress().getHostAddress();
        String mask=calcMaskByPrefixLength(ipAddress.getPrefixLength());
        String gateWay=staticIpConfiguration.gateway.getHostAddress();
        ArrayList<InetAddress> dnsServers = staticIpConfiguration.dnsServers;
        String dns1=dnsServers.get(0).getHostAddress();
        String dns2=dnsServers.get(1).getHostAddress();

    }



    public static List<String>  getEthernetDnsInfo(Context context) {
        List<String> dnsList=new ArrayList<>();
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        List<InetAddress> dnsServers = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            dnsServers = connectivityManager.getLinkProperties(connectivityManager.getActiveNetwork()).getDnsServers();
        }
        for (int i = 0; i < dnsServers.size(); i++) {
            InetAddress inetAddress = dnsServers.get(i);
            String dns = inetAddress.getHostAddress();
            dnsList.add(dns);

        }

        return dnsList;
    }

    /**
     * 解决DhcpInfo获取WiFi子网掩码为0的问题
     * @return
     */
    public static  String getEthernetMask() {
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

    /**
     * 设置以太网动态获取IP
     * @param context
     * @return
     */
    public static boolean setDhcpIpConfiguration(Context context) {
        if (context == null) {
            return false;
        }

        EthernetManager ethernetManager = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            ethernetManager = (EthernetManager) context.getSystemService("ethernet");
        }
        if (ethernetManager == null) {
            return false;
        }

        ethernetManager.setConfiguration("eth0",new IpConfiguration(IpConfiguration.IpAssignment.DHCP, IpConfiguration.ProxySettings.NONE, null, null));
        return true;
    }


    /**
     * 获取是静态还是dhcp
     * @param context
     * @return 1:静态IP，  2：DHCP
     */
    public static int getEthUseDhcpOrStaticIp(Context context) {
        if (context == null) {
            return 0;
        }

        EthernetManager ethernetManager = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            ethernetManager = (EthernetManager) context.getSystemService("ethernet");
        }
        if (ethernetManager == null) {
            return 0;
        }

        IpConfiguration.IpAssignment ipAssignment = ethernetManager.getConfiguration("eth0").ipAssignment;
        if (ipAssignment == IpConfiguration.IpAssignment.STATIC) {
            return 1;
        } else if (ipAssignment == IpConfiguration.IpAssignment.DHCP) {
            return 2;
        }
        return 0;
    }
}
