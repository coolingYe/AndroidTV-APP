package com.zwn.launcher.data.layout;


import com.alibaba.fastjson.annotation.JSONField;

import java.util.List;

public class CategoryBarLayout {
    @JSONField(name = "name")
    public String name;
    @JSONField(name = "code")
    public String code;
    @JSONField(name = "config")
    public ConfigDTO config;
    @JSONField(name = "content")
    public List<ContentDTO> content;

    public static class ConfigDTO {
        @JSONField(name = "showHome")
        public Boolean showHome;
        @JSONField(name = "direction")
        public String direction;
        @JSONField(name = "linkage")
        public Boolean linkage;
    }

    public static class ContentDTO {
        @JSONField(name = "name")
        public String name;
        @JSONField(name = "code")
        public String code;
        @JSONField(name = "appSkus")
        public List<String> appSkus;
    }
}
