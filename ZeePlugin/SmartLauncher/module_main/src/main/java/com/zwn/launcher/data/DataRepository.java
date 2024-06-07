package com.zwn.launcher.data;


import com.zee.guide.data.protocol.response.ServicePkgInfoResp;
import com.zeewain.base.data.protocol.request.UserEventRecordReq;
import com.zeewain.base.data.protocol.response.BaseResp;
import com.zeewain.base.data.protocol.response.DeviceInfoResp;
import com.zeewain.base.utils.RetrofitClient;
import com.zwn.launcher.data.protocol.request.DeviceStatusReportReq;
import com.zwn.launcher.data.protocol.request.SnSoftwareCodeReq;
import com.zwn.launcher.data.protocol.request.UploadLogReq;
import com.zwn.launcher.data.protocol.response.DeviceCheckResp;
import com.zwn.launcher.data.protocol.response.ThemeInfoResp;
import com.zwn.launcher.data.source.http.service.ApiService;
import com.zeewain.base.data.protocol.request.PublishReq;
import com.zeewain.base.data.protocol.request.UpgradeReq;
import com.zeewain.base.data.protocol.response.PublishResp;
import com.zeewain.base.data.protocol.response.UpgradeResp;

import io.reactivex.Observable;
import retrofit2.http.Body;

public class DataRepository implements ApiService{

    private static volatile DataRepository instance;
    private final ApiService apiService;

    private DataRepository(ApiService apiService){
        this.apiService = apiService;
    }

    public static DataRepository getInstance(){
        if(instance == null){
            synchronized (DataRepository.class){
                if (instance == null){
                    instance = new DataRepository(RetrofitClient.getInstance().create(ApiService.class));
                }
            }
        }
        return instance;
    }

    public Observable<BaseResp<PublishResp>> getPublishedVersionInfo(@Body PublishReq publishReq){
        return apiService.getPublishedVersionInfo(publishReq);
    }

    public Observable<BaseResp<UpgradeResp>> getUpgradeVersionInfo(@Body UpgradeReq upgradeReq){
        return apiService.getUpgradeVersionInfo(upgradeReq);
    }

    public Observable<BaseResp<String>> uploadLog(UploadLogReq uploadLogReq) {
        return apiService.uploadLog(uploadLogReq);
    }

    @Override
    public Observable<BaseResp<ServicePkgInfoResp>> getUmsServicePackInfo(String deviceSn) {
        return apiService.getUmsServicePackInfo(deviceSn);
    }

    @Override
    public Observable<BaseResp<DeviceInfoResp>> getDeviceInfo(String deviceSn) {
        return apiService.getDeviceInfo(deviceSn);
    }

    @Override
    public Observable<BaseResp<ThemeInfoResp>> getThemeInfo(String deviceSn) {
        return apiService.getThemeInfo(deviceSn);
    }

    @Override
    public Observable<BaseResp<DeviceCheckResp>> checkDeviceHold(String deviceSn) {
        return apiService.checkDeviceHold(deviceSn);
    }

    @Override
    public Observable<BaseResp<String>> getDeviceHealth(String deviceSn) {
        return apiService.getDeviceHealth(deviceSn);
    }

    @Override
    public Observable<BaseResp<String>> getDeviceOffline(String deviceSn) {
        return apiService.getDeviceOffline(deviceSn);
    }

    @Override
    public Observable<BaseResp<String>> updateAppInstallRecord(SnSoftwareCodeReq snSoftwareCodeReq) {
        return apiService.updateAppInstallRecord(snSoftwareCodeReq);
    }

    @Override
    public Observable<BaseResp<String>> addUserEventRecord(UserEventRecordReq userEventRecordReq) {
        return apiService.addUserEventRecord(userEventRecordReq);
    }

    @Override
    public Observable<BaseResp<String>> reportDeviceStatus(DeviceStatusReportReq deviceStatusReportReq) {
        return apiService.reportDeviceStatus(deviceStatusReportReq);
    }
}
