package com.zee.device.home.data.source.http.service;


import com.zee.device.base.config.BaseConstants;
import com.zee.device.home.data.protocol.request.UpgradeReq;
import com.zee.device.home.data.protocol.request.UserActionRecordReq;
import com.zee.device.home.data.protocol.response.BaseResp;
import com.zee.device.home.data.protocol.response.UpgradeResp;

import retrofit2.http.Body;
import retrofit2.http.POST;
import io.reactivex.Observable;

public interface ApiService {

    @POST(BaseConstants.basePath + BaseConstants.ApiPath.SW_VERSION_NEWER)
    Observable<BaseResp<UpgradeResp>> getUpgradeVersionInfo(@Body UpgradeReq upgradeReq);

    @POST(BaseConstants.basePath + BaseConstants.ApiPath.USER_ACTION_RECORD)
    Observable<BaseResp<String>> addUserActionRecode(@Body UserActionRecordReq userActionRecordReq);
}
