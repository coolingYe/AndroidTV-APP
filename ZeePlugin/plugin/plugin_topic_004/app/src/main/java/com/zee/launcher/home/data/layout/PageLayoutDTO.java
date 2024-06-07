package com.zee.launcher.home.data.layout;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.annotation.JSONField;

import java.util.ArrayList;
import java.util.List;

public class PageLayoutDTO {
    @JSONField(name = "name")
    public String name;

    @JSONField(name = "config")
    public Config config;

    @JSONField(name = "content")
    public JSONArray content;

    public SwiperLayout swiperLayout;
    public List<AppCardListLayout> appCardListLayoutList = new ArrayList<>();

    public static class Config {
        @JSONField(name = "fullName")
        public String fullName;
        @JSONField(name = "preview")
        public String preview;
    }

}
