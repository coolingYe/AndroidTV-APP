package com.zwn.user.data.protocol.request;

public class UserActivateReq {
  public String userCode;
  public String userName;
  public String userPwd;
  public int activateType; //1-代理商激活;2-用户激活
  public String deviceSn;
  public String uuid;
  public String code;

  public UserActivateReq(String userCode, String userPwd, String deviceSn, String uuid, String code) {
    this.userCode = userCode;
    this.userPwd = userPwd;
    this.deviceSn = deviceSn;
    this.uuid = uuid;
    this.code = code;

    this.activateType = 2;
    this.userName = userCode; //use userCode
  }
}
