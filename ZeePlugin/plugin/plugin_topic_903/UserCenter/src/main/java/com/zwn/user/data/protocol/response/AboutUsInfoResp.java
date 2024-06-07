package com.zwn.user.data.protocol.response;

public class AboutUsInfoResp {
    public String wxOfficialAccounts;
    public String logo;
    public String slogan;

    @Override
    public String toString() {
        return "AboutUsResp{" +
                "wxOfficialAccounts='" + wxOfficialAccounts + '\'' +
                ", logo='" + logo + '\'' +
                ", slogan='" + slogan + '\'' +
                '}';
    }
}
