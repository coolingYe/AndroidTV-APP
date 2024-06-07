package com.zee.setting.bean;

public class ConnectedWifi {
    public String name;
    public String password;
    public String usePwdType;

    public ConnectedWifi(String name, String password, String usePwdType) {
        this.name = name;
        this.password = password;
        this.usePwdType = usePwdType;
    }
}
