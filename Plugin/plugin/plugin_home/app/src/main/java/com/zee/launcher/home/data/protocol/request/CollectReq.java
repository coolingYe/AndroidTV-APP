package com.zee.launcher.home.data.protocol.request;

public class CollectReq {
    private String objId;
    private String objName;
    private String objUrl;

    public CollectReq(String objId, String objName, String objUrl) {
        this.objId = objId;
        this.objName = objName;
        this.objUrl = objUrl;
    }

    public String getObjId() {
        return objId;
    }

    public void setObjId(String objId) {
        this.objId = objId;
    }

    public String getObjName() {
        return objName;
    }

    public void setObjName(String objName) {
        this.objName = objName;
    }

    public String getObjUrl() {
        return objUrl;
    }

    public void setObjUrl(String objUrl) {
        this.objUrl = objUrl;
    }
}
