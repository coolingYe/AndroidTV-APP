package com.zee.setting.data;


import com.zee.setting.base.data.protocol.request.PublishReq;
import com.zee.setting.base.data.protocol.response.BaseResp;
import com.zee.setting.base.data.protocol.response.PublishResp;
import com.zee.setting.data.protocol.request.UpgradeReq;
import com.zee.setting.data.protocol.response.AgreementResp;
import com.zee.setting.data.protocol.response.UpgradeResp;
import com.zee.setting.data.service.SettingService;
import com.zee.setting.utils.RetrofitClient;

import io.reactivex.Observable;
import retrofit2.http.Body;

public class SettingRepository {

    private static volatile SettingRepository instance;
    private final SettingService apiService;

    private SettingRepository(SettingService apiService){
        this.apiService = apiService;
    }

    public static SettingRepository getInstance(){
        if(instance == null){
            synchronized (SettingRepository.class){
                if (instance == null){
                    instance = new SettingRepository(RetrofitClient.getInstance().create(SettingService.class));
                }
            }
        }
        return instance;
    }

    public Observable<BaseResp<UpgradeResp>> getUpgradeVersionInfo(@Body UpgradeReq upgradeReq){
        return apiService.getUpgradeVersionInfo(upgradeReq);
    }

    public Observable<BaseResp<PublishResp>> getPublishedVersionInfo(@Body PublishReq publishReq){
        return apiService.getPublishedVersionInfo(publishReq);
    }

    public Observable<BaseResp<AgreementResp>> getAgreementInfo(String agreementCode){
        return apiService.getAgreementInfo(agreementCode);
    }

}
