package com.zee.device.home.data;



import com.zee.device.base.RetrofitClient;
import com.zee.device.home.data.protocol.request.UpgradeReq;
import com.zee.device.home.data.protocol.request.UserActionRecordReq;
import com.zee.device.home.data.protocol.response.BaseResp;
import com.zee.device.home.data.protocol.response.UpgradeResp;
import com.zee.device.home.data.source.http.service.ApiService;

import io.reactivex.Observable;
import retrofit2.http.Body;


public class DataRepository implements ApiService {

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

    public Observable<BaseResp<UpgradeResp>> getUpgradeVersionInfo(@Body UpgradeReq upgradeReq){
        return apiService.getUpgradeVersionInfo(upgradeReq);
    }

    @Override
    public Observable<BaseResp<String>> addUserActionRecode(UserActionRecordReq userActionRecordReq) {
        return apiService.addUserActionRecode(userActionRecordReq);
    }

}
