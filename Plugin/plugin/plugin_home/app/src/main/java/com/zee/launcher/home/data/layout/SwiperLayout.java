package com.zee.launcher.home.data.layout;


import com.alibaba.fastjson.annotation.JSONField;

import java.util.List;

public class SwiperLayout {

    @JSONField(name = "name")
    public String name;
    @JSONField(name = "uid")
    public String uid;
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
        @JSONField(name = "items")
        public List<Item> items;
    }

    public static class Item {
        @JSONField(name = "kind")
        public String kind;
        @JSONField(name = "url")
        public String url;
        @JSONField(name = "id")
        public String id;
        @JSONField(name = "skuId")
        public String skuId;
    }
}
