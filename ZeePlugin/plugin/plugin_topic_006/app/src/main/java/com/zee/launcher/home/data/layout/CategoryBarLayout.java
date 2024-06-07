package com.zee.launcher.home.data.layout;


import com.alibaba.fastjson.annotation.JSONField;

public class CategoryBarLayout {

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
        @JSONField(name = "linkage")
        public Boolean linkage;
        @JSONField(name = "showHome")
        public Boolean showHome;
        @JSONField(name = "direction")
        public String direction;
    }
}
