package com.zwn.launcher.data.layout;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.annotation.JSONField;

import java.util.ArrayList;
import java.util.List;

public class PageLayoutDTO {
    @JSONField(name = "pageType")
    public String pageType;
    @JSONField(name = "config")
    public ConfigDTO config;
    @JSONField(name = "content")
    public List<ContentDTO> content;
    public SwiperLayout swiperLayout;
    public List<AppCardListLayout> appCardListLayoutList = new ArrayList<>();
    public JSONArray subjectJSONArray;
    public String pageName;
    public String pageCode;

    public static class ConfigDTO {
        @JSONField(name = "direction")
        public String direction;
        @JSONField(name = "exclude")
        public List<?> exclude;
    }

    public static class ContentDTO {
        @JSONField(name = "name")
        public String name;
        @JSONField(name = "code")
        public String code;
        @JSONField(name = "config")
        public ConfigDTO config;
        @JSONField(name = "content")
        public JSONArray content;

        public static class ConfigDTO {
            @JSONField(name = "direction")
            public String direction;
        }
    }
}
