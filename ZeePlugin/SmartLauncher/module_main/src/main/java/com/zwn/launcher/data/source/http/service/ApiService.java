package com.zwn.launcher.data.source.http.service;


import com.zee.guide.data.protocol.response.ServicePkgInfoResp;
import com.zeewain.base.config.BaseConstants;
import com.zeewain.base.data.protocol.request.UserEventRecordReq;
import com.zeewain.base.data.protocol.response.BaseResp;
import com.zeewain.base.data.protocol.request.PublishReq;
import com.zeewain.base.data.protocol.request.UpgradeReq;
import com.zeewain.base.data.protocol.response.DeviceInfoResp;
import com.zwn.launcher.data.protocol.request.DeviceStatusReportReq;
import com.zwn.launcher.data.protocol.request.SnSoftwareCodeReq;
import com.zwn.launcher.data.protocol.request.UploadLogReq;
import com.zeewain.base.data.protocol.response.PublishResp;
import com.zwn.launcher.data.protocol.response.DeviceCheckResp;
import com.zwn.launcher.data.protocol.response.ThemeInfoResp;
import com.zeewain.base.data.protocol.response.UpgradeResp;

import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiService {
    @POST(BaseConstants.basePath + BaseConstants.ApiPath.SW_VERSION_LATEST)
    Observable<BaseResp<PublishResp>> getPublishedVersionInfo(@Body PublishReq publishReq);

    @POST(BaseConstants.basePath + BaseConstants.ApiPath.SW_VERSION_NEWER)
    Observable<BaseResp<UpgradeResp>> getUpgradeVersionInfo(@Body UpgradeReq upgradeReq);

    @POST(BaseConstants.basePath + BaseConstants.ApiPath.LOG_UPLOAD)
    Observable<BaseResp<String>> uploadLog(@Body UploadLogReq uploadLogReq);

    @GET(BaseConstants.basePath + BaseConstants.ApiPath.UMS_SERVICE_PACKAGE_INFO)
    Observable<BaseResp<ServicePkgInfoResp>> getUmsServicePackInfo(@Query("deviceSn") String deviceSn);

    @GET(BaseConstants.basePath + BaseConstants.ApiPath.PURCHASE_DEVICE_INFO)
    Observable<BaseResp<DeviceInfoResp>> getDeviceInfo(@Query("deviceSn") String deviceSn);

    @GET(BaseConstants.basePath + BaseConstants.ApiPath.THEME_PACKAGE_INFO)
    Observable<BaseResp<ThemeInfoResp>> getThemeInfo(@Query("deviceSn") String deviceSn);

    @GET(BaseConstants.basePath + BaseConstants.ApiPath.DEVICE_HOLD_CHECK)
    Observable<BaseResp<DeviceCheckResp>> checkDeviceHold(@Query("deviceSn") String deviceSn);

    @GET(BaseConstants.basePath + BaseConstants.ApiPath.DEVICE_HEALTH)
    Observable<BaseResp<String>> getDeviceHealth(@Query("deviceSn") String deviceSn);

    @GET(BaseConstants.basePath + BaseConstants.ApiPath.DEVICE_OFFLINE)
    Observable<BaseResp<String>> getDeviceOffline(@Query("deviceSn") String deviceSn);

    @POST(BaseConstants.basePath + BaseConstants.ApiPath.APP_INSTALL_RECORD)
    Observable<BaseResp<String>> updateAppInstallRecord(@Body SnSoftwareCodeReq snSoftwareCodeReq);

    @POST(BaseConstants.basePath + BaseConstants.ApiPath.ADD_USER_EVENT_RECORD)
    Observable<BaseResp<String>> addUserEventRecord(@Body UserEventRecordReq userEventRecordReq);

    @POST(BaseConstants.basePath + BaseConstants.ApiPath.DEVICE_STATUS_REPORT)
    Observable<BaseResp<String>> reportDeviceStatus(@Body DeviceStatusReportReq deviceStatusReportReq);
}
