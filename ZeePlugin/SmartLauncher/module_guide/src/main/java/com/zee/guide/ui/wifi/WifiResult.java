package com.zee.guide.ui.wifi;


public class WifiResult {
    public static final int CARE_TYPE_SELECTED = 0;
    public static final int CARE_TYPE_SAVED_DISABLED = 1;
    public static final int CARE_TYPE_SAVED_ENABLED = 2;
    public static final int CARE_TYPE_UNSAVED = -1;
    public String mac;
    public String name;
    public String capabilities;
    public int signalLevel;
    public int careType = CARE_TYPE_UNSAVED; //0 selected, 1 saved & DISABLED; 2 saved & ENABLED; -1 unsaved
    public int itemType = 0; //wifi common item

    public WifiResult(String mac, int itemType) {
        this.mac = mac;
        this.itemType = itemType;
    }

    public WifiResult(String mac, String name, String capabilities, int signalLevel) {
        this.mac = mac;
        this.name = name;
        this.capabilities = capabilities;
        this.signalLevel = signalLevel;
    }

    public WifiResult(String mac, String name, String capabilities, int signalLevel, int careType) {
        this.mac = mac;
        this.name = name;
        this.capabilities = capabilities;
        this.signalLevel = signalLevel;
        this.careType = careType;
    }

    @Override
    public String toString() {
        return "WifiResult{" +
                "mac='" + mac + '\'' +
                ", name='" + name + '\'' +
                ", capabilities='" + capabilities + '\'' +
                ", signalLevel=" + signalLevel +
                ", careType=" + careType +
                '}';
    }
}
