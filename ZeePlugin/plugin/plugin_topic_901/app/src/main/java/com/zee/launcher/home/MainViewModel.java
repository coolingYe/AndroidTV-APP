package com.zee.launcher.home;


import android.annotation.SuppressLint;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.google.gson.Gson;
import com.zee.launcher.home.data.DataRepository;
import com.zee.launcher.home.data.protocol.request.TouristLoginReq;
import com.zee.launcher.home.data.layout.AppCardListLayout;
import com.zee.launcher.home.data.layout.GlobalLayout;
import com.zee.launcher.home.data.layout.PageLayoutDTO;
import com.zee.launcher.home.data.protocol.response.AkSkResp;
import com.zee.launcher.home.data.protocol.response.ServicePkgInfoResp;
import com.zee.launcher.home.utils.GlobalLayoutHelper;
import com.zeewain.base.BaseApplication;
import com.zeewain.base.config.BaseConstants;
import com.zeewain.base.config.SharePrefer;
import com.zeewain.base.data.protocol.response.BaseResp;
import com.zeewain.base.model.LoadState;
import com.zeewain.base.ui.BaseViewModel;
import com.zeewain.base.utils.CommonUtils;
import com.zeewain.base.utils.SPUtils;
import com.zeewain.base.data.protocol.request.AkSkReq;
import com.zeewain.base.data.protocol.response.LoginResp;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

public class MainViewModel extends BaseViewModel {
    private final DataRepository dataRepository;

    public GlobalLayout globalLayout;
    public MutableLiveData<LoadState> mldServicePackInfoLoadState = new MutableLiveData<>();
    public MutableLiveData<LoadState> mldTouristLoginState = new MutableLiveData<>();
    public MutableLiveData<String> mldToastMsg = new MutableLiveData<>();
    public String knowledgeQuizSkuId;
    public int tryReqServicePackInfoTimes = 3;

    public MainViewModel(DataRepository dataRepository) {
        this.dataRepository = dataRepository;
    }

    public List<String> getSkuIdListByPageName(String pageName){
        if(globalLayout.layout.pages != null){
            for(int i=0; i<globalLayout.layout.pages.size(); i++){
                PageLayoutDTO pageLayoutDTO = globalLayout.layout.pages.get(i);
                if(pageName.equals(pageLayoutDTO.name)){
                    if(pageLayoutDTO.appCardListLayoutList.size() > 0 ){
                        AppCardListLayout appCardListLayout = pageLayoutDTO.appCardListLayoutList.get(0);
                        if(appCardListLayout.config != null && appCardListLayout.config.appSkus != null){
                            return appCardListLayout.config.appSkus;
                        }
                    }
                }
            }
        }
        return new ArrayList<>();
    }

    private String getKnowledgeQuizSkuId(){
        if(globalLayout.layout.pages != null){
            for(int i=0; i<globalLayout.layout.pages.size(); i++){
                PageLayoutDTO pageLayoutDTO = globalLayout.layout.pages.get(i);
                if("首页".equals(pageLayoutDTO.name)){
                    if(pageLayoutDTO.appCardListLayoutList.size() > 0 ){
                        AppCardListLayout appCardListLayout = pageLayoutDTO.appCardListLayoutList.get(0);
                        if(appCardListLayout.config != null && appCardListLayout.config.appSkus != null && appCardListLayout.config.appSkus.size() > 0){
                            return appCardListLayout.config.appSkus.get(0);
                        }
                    }
                }
            }
        }
        return null;
    }

    public void reqServicePackInfo() {
        tryReqServicePackInfoTimes --;
        mldServicePackInfoLoadState.setValue(LoadState.Loading);
        dataRepository.getServicePackInfo(CommonUtils.getDeviceSn())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(this)
                .subscribe(new DisposableObserver<BaseResp<ServicePkgInfoResp>>() {
                    @Override
                    public void onNext(@NonNull BaseResp<ServicePkgInfoResp> response) {
                        if (response.code == BaseConstants.API_HANDLE_SUCCESS) {
                            globalLayout = GlobalLayoutHelper.analysisGlobalLayout(response.data.layoutJson);
                            if(globalLayout != null){
                                knowledgeQuizSkuId = getKnowledgeQuizSkuId();
                                if(knowledgeQuizSkuId != null){
                                    mldServicePackInfoLoadState.setValue(LoadState.Success);
                                }else{
                                    mldServicePackInfoLoadState.setValue(LoadState.Failed);
                                    if(tryReqServicePackInfoTimes == 0)
                                        mldToastMsg.setValue("服务包配置错误！");
                                }
                            }else{
                                mldServicePackInfoLoadState.setValue(LoadState.Failed);
                                if(tryReqServicePackInfoTimes == 0)
                                    mldToastMsg.setValue("服务包配置错误！");
                            }
                        }else{
                            if(tryReqServicePackInfoTimes == 0)
                                mldToastMsg.setValue(response.message);
                            mldServicePackInfoLoadState.setValue(LoadState.Failed);
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        mldServicePackInfoLoadState.setValue(LoadState.Failed);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    public void reqTouristLogin(String deviceSn) {
        TouristLoginReq touristLoginReq = new TouristLoginReq(deviceSn);
        mldTouristLoginState.setValue(LoadState.Loading);
        dataRepository.touristLoginReq(touristLoginReq)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(this)
                .subscribe(new DisposableObserver<BaseResp<LoginResp>>() {
                    @Override
                    public void onNext(@NonNull BaseResp<LoginResp> resp) {
                        if (resp.code == BaseConstants.API_HANDLE_SUCCESS) {
                            BaseApplication.userToken = resp.data.token;
                            reqAkSkInfo(resp.data.token);
                        } else {
                            mldTouristLoginState.setValue(LoadState.Failed);
                            mldToastMsg.setValue(resp.message);
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        mldTouristLoginState.setValue(LoadState.Failed);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    public void reqAkSkInfo(String userToken) {
        dataRepository.getAkSkInfo(new AkSkReq(BaseConstants.AUTH_SYSTEM_CODE))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(this)
                .subscribe(new DisposableObserver<BaseResp<AkSkResp>>() {
                    @Override
                    public void onNext(@NonNull BaseResp<AkSkResp> resp) {
                        if (resp.code == BaseConstants.API_HANDLE_SUCCESS) {
                            AkSkResp akSkResp = resp.data;
                            if (akSkResp != null && akSkResp.akCode != null && !akSkResp.akCode.isEmpty()) {
                                Gson gson = new Gson();
                                String akSkString = gson.toJson(akSkResp);
                                saveLoginInfo(SharePrefer.akSkInfo, akSkString);
                                saveLoginInfo(SharePrefer.userToken, userToken);
                                if(isTokenAndAkSkExist()) {
                                    mldTouristLoginState.setValue(LoadState.Success);
                                }else{
                                    mldTouristLoginState.setValue(LoadState.Failed);
                                    BaseApplication.userToken = "";
                                }
                            } else {
                                mldTouristLoginState.setValue(LoadState.Failed);
                                saveLoginInfo(SharePrefer.userToken, "");
                                BaseApplication.userToken = "";
                                mldToastMsg.setValue("AK&SK配置错误！");
                            }
                        }else{
                            mldTouristLoginState.setValue(LoadState.Failed);
                            saveLoginInfo(SharePrefer.userToken, "");
                            BaseApplication.userToken = "";
                            mldToastMsg.setValue(resp.message);
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        saveLoginInfo(SharePrefer.userToken, "");
                        BaseApplication.userToken = "";
                        mldTouristLoginState.setValue(LoadState.Failed);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    private boolean isTokenAndAkSkExist(){
        String userToken = SPUtils.getInstance().getString(SharePrefer.userToken);
        String akSkInfo = SPUtils.getInstance().getString(SharePrefer.akSkInfo);
        return (userToken != null && !userToken.isEmpty() && akSkInfo != null && !akSkInfo.isEmpty());
    }

    private void saveLoginInfo(String key, String value) {
        SPUtils.getInstance().put(key, value);
    }
}
