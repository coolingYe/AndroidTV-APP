package com.zee.setting.data.service;

import com.zee.setting.base.BaseConstants;
import com.zee.setting.base.data.protocol.request.PublishReq;
import com.zee.setting.base.data.protocol.response.BaseResp;
import com.zee.setting.base.data.protocol.response.PublishResp;
import com.zee.setting.data.protocol.request.UpgradeReq;
import com.zee.setting.data.protocol.response.AgreementResp;
import com.zee.setting.data.protocol.response.UpgradeResp;

import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface SettingService {

    @POST(BaseConstants.basePath + BaseConstants.ApiPath.SW_VERSION_NEWER)
    Observable<BaseResp<UpgradeResp>> getUpgradeVersionInfo(@Body UpgradeReq upgradeReq);

    @POST(BaseConstants.basePath + BaseConstants.ApiPath.SW_VERSION_LATEST)
    Observable<BaseResp<PublishResp>> getPublishedVersionInfo(@Body PublishReq publishReq);

    @GET(BaseConstants.basePath + BaseConstants.ApiPath.APP_AGREEMENT)
    Observable<BaseResp<AgreementResp>> getAgreementInfo(@Query("agreementCode") String agreementCode);
}
