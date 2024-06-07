package com.zee.launcher.home.gesture.model;

import java.io.Serializable;

public class AkSkResp implements Serializable {
    public String akCode;
    public String skCode;
    public String authVersion;

    @Override
    public String toString() {
        return "AkSkResp{" +
                "akCode='" + akCode + '\'' +
                ", skCode='" + skCode + '\'' +
                ", authVersion='" + authVersion + '\'' +
                '}';
    }
}
