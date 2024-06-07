package com.zee.launcher.home.data.layout;


import com.alibaba.fastjson.annotation.JSONField;

import java.util.List;

public class AppCardListLayout {
    @JSONField(name = "name")
    public String name;
    @JSONField(name = "code")
    public String code;
    @JSONField(name = "config")
    public ConfigDTO config;

    public static class ConfigDTO {
        @JSONField(name = "appSkus")
        public List<String> appSkus;
        @JSONField(name = "title")
        public String title;
        @JSONField(name = "pageSize")
        public Integer pageSize;
        @JSONField(name = "direction")
        public String direction;
        @JSONField(name = "rowCount")
        public Integer rowCount;
        @JSONField(name = "layout")
        public String layout;
        @JSONField(name = "previewDisplay")
        public String previewDisplay;
        @JSONField(name = "showAppName")
        public Boolean showAppName;
        @JSONField(name = "showAppDesc")
        public Boolean showAppDesc;
    }
}
