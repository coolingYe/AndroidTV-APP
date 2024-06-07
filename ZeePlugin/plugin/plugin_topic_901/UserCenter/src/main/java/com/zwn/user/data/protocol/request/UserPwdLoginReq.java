package com.zwn.user.data.protocol.request;

public class UserPwdLoginReq {
    public String loginName;
    public String userPwd;
    public String appCode;
    public String deviceSn;
    public String uuid;
    public String code;

    public UserPwdLoginReq(String loginName, String userPwd, String deviceSn, String uuid, String code) {
        this.loginName = loginName;
        this.userPwd = userPwd;
        this.appCode = "mall_ums";
        this.deviceSn = deviceSn;
        this.uuid = uuid;
        this.code = code;
    }
}
