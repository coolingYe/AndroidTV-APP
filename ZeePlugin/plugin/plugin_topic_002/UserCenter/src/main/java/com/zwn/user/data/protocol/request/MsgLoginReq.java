package com.zwn.user.data.protocol.request;

public class MsgLoginReq {
    public String type;
    public String telephone;
    public String code;
    public String uuid;

    public MsgLoginReq(String type, String telephone, String code, String uuid) {
        this.type = type;
        this.telephone = telephone;
        this.code = code;
        this.uuid = uuid;
    }
}
