package com.zwn.user.data.model;

public class SessionInfoMo {
    public String sessionId;
    public String loginPageUrl;
    public String sessionSecretKey;
    public long createTime;

    public SessionInfoMo(String sessionId, String loginPageUrl, String sessionSecretKey, long createTime) {
        this.sessionId = sessionId;
        this.loginPageUrl = loginPageUrl;
        this.sessionSecretKey = sessionSecretKey;
        this.createTime = createTime;
    }
}
