package com.zee.launcher.login.data.protocol.request;

public class UserActivateReq {
    public String userCode;
    public String telephone;
    public String userName;
    public String userPwd;
    public String deviceSn;
    public String uuid;
    public String code;
    public int expirationTime = 10512000;
    public String selectUserCode;
    public String userType = "2"; //用户类型 2-个体 4-游客

    public UserActivateReq(String userCode, String telephone, String userPwd, String deviceSn, String uuid, String code) {
        this.userCode = userCode;
        this.telephone = telephone;
        this.userPwd = userPwd;
        this.deviceSn = deviceSn;
        this.uuid = uuid;
        this.code = code;
    }
}
