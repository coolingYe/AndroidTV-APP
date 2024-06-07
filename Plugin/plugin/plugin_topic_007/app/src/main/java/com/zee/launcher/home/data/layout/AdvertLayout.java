package com.zee.launcher.home.data.layout;


import com.alibaba.fastjson.annotation.JSONField;

import java.util.List;

public class AdvertLayout {

    @JSONField(name = "id")
    public String id;
    @JSONField(name = "uid")
    public String uid;
    @JSONField(name = "code")
    public String code;
    @JSONField(name = "icon")
    public String icon;
    @JSONField(name = "name")
    public String name;
    @JSONField(name = "index")
    public Integer index;
    @JSONField(name = "config")
    public ConfigDTO config;

    public static class ConfigDTO {
        @JSONField(name = "layout")
        public String layout;
        @JSONField(name = "previewDisplay")
        public String previewDisplay;
        @JSONField(name = "showAppName")
        public Boolean showAppName;
        @JSONField(name = "rowCount")
        public Integer rowCount;
        @JSONField(name = "appSkus")
        public List<String> appSkus;
        @JSONField(name = "title")
        public String title;
        @JSONField(name = "showAppDesc")
        public Boolean showAppDesc;
    }
}
