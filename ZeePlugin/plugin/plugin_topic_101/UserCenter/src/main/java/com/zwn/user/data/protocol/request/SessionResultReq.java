package com.zwn.user.data.protocol.request;

public class SessionResultReq {
    public String sessionId;
    public String sessionSecretKey;

    public SessionResultReq(String sessionId, String sessionSecretKey) {
        this.sessionId = sessionId;
        this.sessionSecretKey = sessionSecretKey;
    }
}
