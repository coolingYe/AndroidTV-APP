package com.zwn.launcher.data.layout;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.annotation.JSONField;

import java.util.List;

public class GlobalLayout {

    @JSONField(name = "global")
    public GlobalDTO global;
    @JSONField(name = "layout")
    public LayoutDTO layout;

    public static class GlobalDTO {
        @JSONField(name = "showFloatTool")
        public Boolean showFloatTool;
    }

    public static class LayoutDTO {
        @JSONField(name = "basic")
        public BasicDTO basic;
        @JSONField(name = "pages")
        public List<PageLayoutDTO> pages;

        public static class BasicDTO {
            @JSONField(name = "name")
            public String name;
            @JSONField(name = "code")
            public String code;
            @JSONField(name = "config")
            public ConfigDTO config;
            @JSONField(name = "content")
            public JSONArray content;
            public PageHeaderLayout pageHeaderLayout;
            public CategoryBarLayout categoryBarLayout;

            public static class ConfigDTO {
                @JSONField(name = "direction")
                public String direction;
            }
        }
    }
}
