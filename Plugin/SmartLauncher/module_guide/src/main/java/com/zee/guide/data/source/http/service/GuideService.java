package com.zee.guide.data.source.http.service;

import com.zeewain.base.data.protocol.request.UpgradeReq;
import com.zeewain.base.data.protocol.response.DeviceInfoResp;
import com.zee.guide.data.protocol.response.ServicePkgInfoResp;
import com.zeewain.base.config.BaseConstants;
import com.zeewain.base.data.protocol.response.BaseResp;
import com.zeewain.base.data.protocol.response.UpgradeResp;

import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface GuideService {

    @GET(BaseConstants.basePath + BaseConstants.ApiPath.PURCHASE_DEVICE_INFO)
    Observable<BaseResp<DeviceInfoResp>> getDeviceInfo(@Query("deviceSn") String deviceSn);

    @GET(BaseConstants.basePath + BaseConstants.ApiPath.SERVICE_PACKAGE_INFO)
    Observable<BaseResp<ServicePkgInfoResp>> getServicePackInfo(@Query("deviceSn") String deviceSn);

    @POST(BaseConstants.basePath + BaseConstants.ApiPath.SW_VERSION_NEWER)
    Observable<BaseResp<UpgradeResp>> getUpgradeVersionInfo(@Body UpgradeReq upgradeReq);
}
