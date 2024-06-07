package com.zee.launcher.login.data.protocol.request;

public class UserPwdLoginReq {
    public String loginName;
    public String userPwd;
    public String appCode;
    public String deviceSn;
    public String uuid;
    public String code;
    public String type;//"0" 账号密码， "1" 手机号
    public int expirationTime = 10512000;
    public String selectUserCode;

    public UserPwdLoginReq(String loginName, String userPwd, String deviceSn, String uuid, String code, String type) {
        this.loginName = loginName;
        this.userPwd = userPwd;
        this.appCode = "mall_ums";
        this.deviceSn = deviceSn;
        this.uuid = uuid;
        this.code = code;
        this.type = type;
    }
}


