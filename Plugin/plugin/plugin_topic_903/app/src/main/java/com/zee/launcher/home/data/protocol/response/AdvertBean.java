package com.zee.launcher.home.data.protocol.response;


import java.util.ArrayList;
import java.util.List;

public class AdvertBean {

    public String id;
    public String uid;
    public String code;
    public String icon;
    public String name;
    public Integer index;
    public ConfigDTO config = new ConfigDTO();

    public static class ConfigDTO {
        public String layout;
        public String previewDisplay;
        public Boolean showAppName;
        public Integer rowCount;
        public List<String> appSkus = new ArrayList<>();
        public String title;
        public Boolean showAppDesc;
    }
}
