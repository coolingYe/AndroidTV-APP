package com.zee.launcher.home.data.layout;


import com.alibaba.fastjson.annotation.JSONField;

public class PageHeaderLayout {
    @JSONField(name = "name")
    public String name;
    @JSONField(name = "code")
    public String code;
    @JSONField(name = "config")
    public ConfigDTO config;

    public static class ConfigDTO {
        @JSONField(name = "showLogo")
        public Boolean showLogo;
        @JSONField(name = "showLogoText")
        public Boolean showLogoText;
    }
}
