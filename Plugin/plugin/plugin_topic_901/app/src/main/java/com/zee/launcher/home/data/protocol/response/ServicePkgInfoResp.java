package com.zee.launcher.home.data.protocol.response;

import com.alibaba.fastjson.JSONObject;

public class ServicePkgInfoResp {
    /**
     * 布局详情信息
     */
    public JSONObject layoutJson;

    /**
     * 服务包ID
     */
    public int packId;

    /**
     * 服务包名称
     */
    public String packName;

    /**
     * 服务包类型,0-资源包,1-基础包,2-高级包,3-尊享包
     */
    public int packType;
}
