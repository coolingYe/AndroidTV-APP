package com.zwn.user.data.protocol.request;

public class ResetPasswordReq {
    public String telephone;
    public String newPassword;
    public String confirmPassword;
    public String uuid;

    public ResetPasswordReq(String telephone, String newPassword, String confirmPassword, String uuid) {
        this.telephone = telephone;
        this.newPassword = newPassword;
        this.confirmPassword = confirmPassword;
        this.uuid = uuid;
    }
}
