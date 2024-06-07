package com.zee.launcher.login.data.protocol.request;

import java.util.List;

public class ImageCaptchaReq {
    public String type;//0-发送短信验证码 1-用户登录 2-登录密码校验 3-用户注册 4-小程序
    public List<String> types;

    public ImageCaptchaReq(String type) {
        this.type = type;
    }

    public ImageCaptchaReq(List<String> types) {
        this.types = types;
    }
}
