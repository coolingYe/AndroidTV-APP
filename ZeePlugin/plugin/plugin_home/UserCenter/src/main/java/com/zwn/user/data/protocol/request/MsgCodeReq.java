package com.zwn.user.data.protocol.request;

public class MsgCodeReq {
    public String type;
    public String telephone;

    public MsgCodeReq(String type, String telephone) {
        this.type = type;
        this.telephone = telephone;
    }
}
