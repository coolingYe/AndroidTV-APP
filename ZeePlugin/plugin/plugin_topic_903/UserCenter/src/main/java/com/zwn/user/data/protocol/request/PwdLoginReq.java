package com.zwn.user.data.protocol.request;

public class PwdLoginReq {
    public String type;
    public String loginName;
    public String password;

    public PwdLoginReq(String type, String loginName, String password) {
        this.type = type;
        this.loginName = loginName;
        this.password = password;
    }
}
