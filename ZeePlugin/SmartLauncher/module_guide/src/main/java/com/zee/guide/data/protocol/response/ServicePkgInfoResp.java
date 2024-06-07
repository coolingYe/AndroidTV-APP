package com.zee.guide.data.protocol.response;


import com.alibaba.fastjson.JSONObject;

import java.util.List;

public class ServicePkgInfoResp {

    /**
     * 布局详情信息
     */
    public JSONObject themeJson;

    /**
     * 服务包ID
     */
    public int packId;

    /**
     * 附加功能json
     */
    public ExtendJsonDTO extendJson;

    /**
     * 课件sku列表
     */
    public List<String> coursewareSku;

    public static class ExtendJsonDTO {
        // 登录模式：1=宿主登录、2=主题内登录
        public int loginMode;
        public AiGestureDTO aiGesture;

        public static class AiGestureDTO{
            public boolean enabled;
        }
    }

}


