package com.zwn.user.data.protocol.response;

import java.io.Serializable;

public class AkSkResp implements Serializable {
    public String akCode;
    public String skCode;
    public String authVersion;

    @Override
    public String toString() {
        return "AkSkResp{" +
                "akCode='" + akCode + '\'' +
                ", skCode='" + skCode + '\'' +
                ", authVersion='" + authVersion + '\'' +
                '}';
    }
}
