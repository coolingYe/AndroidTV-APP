package com.zee.setting.bean;

public class WifiDhcp {
    public String ipv4;
    public String netmask;
    public String gateway;
    public String dns;

    public WifiDhcp(String ipv4, String netmask, String gateway, String dns) {
        this.ipv4 = ipv4;
        this.netmask = netmask;
        this.gateway = gateway;
        this.dns = dns;
    }

    @Override
    public String toString() {
        return "WifiDhcp{" +
                "ipv4='" + ipv4 + '\'' +
                ", netmask='" + netmask + '\'' +
                ", gateway='" + gateway + '\'' +
                ", dns='" + dns + '\'' +
                '}';
    }
}
