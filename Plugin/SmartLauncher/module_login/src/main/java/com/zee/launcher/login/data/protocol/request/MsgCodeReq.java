package com.zee.launcher.login.data.protocol.request;

public class MsgCodeReq {
    public String type;
    public String telephone;
    public String uuid;
    public String code;

    public MsgCodeReq(String type, String telephone, String uuid, String code) {
        this.type = type;
        this.telephone = telephone;
        this.uuid = uuid;
        this.code = code;
    }
}
