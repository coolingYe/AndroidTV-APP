package com.zee.launcher.home.data.layout;


import com.alibaba.fastjson.annotation.JSONField;

import java.util.List;

public class SwiperLayout {

    @JSONField(name = "name")
    public String name;
    @JSONField(name = "code")
    public String code;
    @JSONField(name = "config")
    public ConfigDTO config;

    public static class ConfigDTO {
        @JSONField(name = "displayMode")
        public String displayMode;
        @JSONField(name = "appSkus")
        public List<String> appSkus;
        @JSONField(name = "viewSize")
        public Integer viewSize;
        @JSONField(name = "maxCount")
        public Integer maxCount;
        @JSONField(name = "interval")
        public Integer interval;
        @JSONField(name = "effect")
        public String effect;
    }
}
