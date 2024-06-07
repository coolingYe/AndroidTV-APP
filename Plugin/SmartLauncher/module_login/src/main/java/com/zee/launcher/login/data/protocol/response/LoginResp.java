package com.zee.launcher.login.data.protocol.response;

import java.util.List;

public class LoginResp {
    public String token;
    public List<UserInfoResp> userOptionList;

    @Override
    public String toString() {
        return "LoginResp{" +
                "token='" + token + '\'' +
                ", userOptionList=" + userOptionList +
                '}';
    }

}
