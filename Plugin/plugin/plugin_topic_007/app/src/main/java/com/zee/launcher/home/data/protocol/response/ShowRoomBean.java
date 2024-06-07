package com.zee.launcher.home.data.protocol.response;


import java.util.List;

public class ShowRoomBean {

    public String id;
    public String uid;
    public String code;
    public String icon;
    public String name;
    public Integer index;
    public ConfigDTO config;

    public static class ConfigDTO {
        public List<ItemDTO> items;


    }

    public static class ItemDTO {
        public String kind;
        public String id;
        public String url;
        public String skuId;

    }
}
