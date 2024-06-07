package com.zee.launcher.home;


import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.google.gson.Gson;
import com.zee.launcher.home.data.DataRepository;
import com.zee.launcher.home.data.layout.AppCardListLayout;
import com.zee.launcher.home.data.layout.GlobalLayout;
import com.zee.launcher.home.data.layout.PageLayoutDTO;
import com.zee.launcher.home.data.model.UnitMo;
import com.zee.launcher.home.data.protocol.request.OrganizerInfoListReq;
import com.zee.launcher.home.data.protocol.response.OrganizerInfoResp;
import com.zee.launcher.home.data.protocol.response.ServicePkgInfoResp;
import com.zee.launcher.home.utils.GlobalLayoutHelper;
import com.zeewain.base.config.BaseConstants;
import com.zeewain.base.config.SharePrefer;
import com.zeewain.base.data.protocol.response.BaseResp;
import com.zeewain.base.model.DataLoadState;
import com.zeewain.base.model.LoadState;
import com.zeewain.base.ui.BaseViewModel;
import com.zeewain.base.utils.CommonUtils;
import com.zeewain.base.utils.SPUtils;
import com.zwn.user.data.model.SessionInfoMo;
import com.zwn.user.data.model.SessionResultLoadState;
import com.zwn.user.data.protocol.request.AkSkReq;
import com.zwn.user.data.protocol.request.DeviceSNReq;
import com.zwn.user.data.protocol.request.SessionResultReq;
import com.zwn.user.data.protocol.response.AkSkResp;
import com.zwn.user.data.protocol.response.CreateSessionResp;
import com.zwn.user.data.protocol.response.LoginResp;
import com.zwn.user.data.protocol.response.UserInfoResp;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

public class MainViewModel extends BaseViewModel {
    private final DataRepository dataRepository;
    public GlobalLayout globalLayout;
    public MutableLiveData<LoadState> mldServicePackInfoLoadState = new MutableLiveData<>();
    public MutableLiveData<LoadState> mldOrganizerInfoListLoadState = new MutableLiveData<>();
    public MutableLiveData<LoadState> mldAkSkInfoLoadState = new MutableLiveData<>();
    public MutableLiveData<LoadState> mldUserInfoLoadState = new MutableLiveData<>();
    public MutableLiveData<DataLoadState<CreateSessionResp>> mldCreateLoginSessionLoadState = new MutableLiveData<>();
    public MutableLiveData<SessionResultLoadState> mldReqSessionResultLoadState = new MutableLiveData<>();
    public static final ConcurrentHashMap<String, SessionInfoMo> reqSessionResultMap = new ConcurrentHashMap<>();
    public MutableLiveData<String> mldToastMsg = new MutableLiveData<>();
    public int tryReqServicePackInfoTimes = 3;
    public int tryReqOrganizerInfoListTimes = 3;
    public int tryReqAkSkInfoTimes = 3;
    public int tryReqUserInfoTimes = 3;
    public List<OrganizerInfoResp> organizerInfoList;
    public String baiYunLakeSkuId;
    public String userName;

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

    private String getBaiYunLakeSkuId(){
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
                                baiYunLakeSkuId = getBaiYunLakeSkuId();
                                if(baiYunLakeSkuId != null){
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

    public void reqOrganizerInfoList() {
        tryReqOrganizerInfoListTimes --;
        mldOrganizerInfoListLoadState.setValue(LoadState.Loading);
        dataRepository.getOrganizerInfoList(new OrganizerInfoListReq())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(this)
                .subscribe(new DisposableObserver<BaseResp<List<OrganizerInfoResp>>>() {
                    @Override
                    public void onNext(@NonNull BaseResp<List<OrganizerInfoResp>> response) {
                        if (response.code == BaseConstants.API_HANDLE_SUCCESS) {
                            organizerInfoList = response.data;
                            mldOrganizerInfoListLoadState.setValue(LoadState.Success);
                        }else{
                            if(tryReqOrganizerInfoListTimes == 0) {
                                mldToastMsg.setValue(response.message);
                            }
                            mldOrganizerInfoListLoadState.setValue(LoadState.Failed);
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        mldOrganizerInfoListLoadState.setValue(LoadState.Failed);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    public List<List<UnitMo>> getUnitMoList(){
        List<List<UnitMo>> unitMoDataList = new ArrayList<>();
        if(organizerInfoList != null){
            List<UnitMo> unitMoList = null;
            for(int i=0; i<organizerInfoList.size(); i++){
                OrganizerInfoResp info = organizerInfoList.get(i);
                if(i % 4 == 0){
                    unitMoList = new ArrayList<>();
                    unitMoDataList.add(unitMoList);
                    unitMoList.add(new UnitMo(info.organizerLogo));
                }else{
                    unitMoList.add(new UnitMo(info.organizerLogo));
                }
            }
        }

        if(unitMoDataList.size() == 1){
            unitMoDataList.add(unitMoDataList.get(0));
        }
        return unitMoDataList;
    }

    public void reqAkSkInfo() {
        tryReqAkSkInfoTimes --;
        mldAkSkInfoLoadState.setValue(LoadState.Loading);
        dataRepository.getAkSkInfo(new AkSkReq(BaseConstants.AUTH_SYSTEM_CODE))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(this)
                .subscribe(new DisposableObserver<BaseResp<AkSkResp>>() {
                    @Override
                    public void onNext(@NonNull BaseResp<AkSkResp> response) {
                        if (response.code == BaseConstants.API_HANDLE_SUCCESS) {
                            if (response.data != null && response.data.akCode != null && !response.data.akCode.isEmpty()) {
                                Gson gson = new Gson();
                                String akSkString = gson.toJson(response.data);
                                SPUtils.getInstance().put(SharePrefer.akSkInfo, akSkString);
                                mldAkSkInfoLoadState.setValue(LoadState.Success);
                            }else{
                                mldAkSkInfoLoadState.setValue(LoadState.Failed);
                            }
                        }else{
                            if(tryReqAkSkInfoTimes == 0) {
                                mldToastMsg.setValue(response.message);
                            }
                            mldAkSkInfoLoadState.setValue(LoadState.Failed);
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        mldAkSkInfoLoadState.setValue(LoadState.Failed);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    public void reqUserInfo() {
        tryReqUserInfoTimes --;
        mldUserInfoLoadState.setValue(LoadState.Loading);
        dataRepository.getUserInfo()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(this)
                .subscribe(new DisposableObserver<BaseResp<UserInfoResp>>() {
                    @Override
                    public void onNext(BaseResp<UserInfoResp> response) {
                        if (response.code == BaseConstants.API_HANDLE_SUCCESS) {
                            if (response.data != null) {
                                userName = response.data.getUserName();
                            }
                            mldUserInfoLoadState.setValue(LoadState.Success);
                        } else {
                            if(tryReqUserInfoTimes == 0) {
                                mldToastMsg.setValue(response.message);
                            }
                            mldUserInfoLoadState.setValue(LoadState.Failed);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        mldUserInfoLoadState.setValue(LoadState.Failed);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    public synchronized void addReqSessionResult(SessionInfoMo sessionInfoMo){
        if(reqSessionResultMap.size() < 5){
            reqSessionResultMap.put(sessionInfoMo.sessionId, sessionInfoMo);
        }else{
            String earliestSession = null;
            long earliestTime = System.currentTimeMillis();
            for (Map.Entry<String, SessionInfoMo> entry: reqSessionResultMap.entrySet()) {
                if(entry.getValue().createTime < earliestTime){
                    earliestTime = entry.getValue().createTime;
                    earliestSession = entry.getKey();
                }
            }

            if(earliestSession != null){
                reqSessionResultMap.remove(earliestSession);
            }

            reqSessionResultMap.put(sessionInfoMo.sessionId, sessionInfoMo);
        }
    }

    public void checkValidSessionAndReq(){
        long earliestTime = System.currentTimeMillis() - 10 * 60 * 1000;
        for (Map.Entry<String, SessionInfoMo> entry: reqSessionResultMap.entrySet()) {
            if(entry.getValue().createTime > earliestTime ){
                reqSessionResult(entry.getValue());
            }
        }
    }

    public void createLoginSession() {
        mldCreateLoginSessionLoadState.setValue(new DataLoadState<>(LoadState.Loading));
        dataRepository.createSession(new DeviceSNReq(CommonUtils.getDeviceSn()))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(this)
                .subscribe(new DisposableObserver<BaseResp<CreateSessionResp>>() {
                    @Override
                    public void onNext(@NonNull BaseResp<CreateSessionResp> response) {
                        if(BaseConstants.API_HANDLE_SUCCESS == response.code){
                            if(response.data != null && response.data.sessionId != null && !response.data.sessionId.isEmpty()){
                                mldCreateLoginSessionLoadState.setValue(new DataLoadState<>(LoadState.Success, response.data));
                            }else{
                                mldCreateLoginSessionLoadState.setValue(new DataLoadState<>(LoadState.Failed));
                            }
                        }else{
                            mldToastMsg.setValue(response.message);
                            mldCreateLoginSessionLoadState.setValue(new DataLoadState<>(LoadState.Failed));
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        mldCreateLoginSessionLoadState.setValue(new DataLoadState<>(LoadState.Failed));
                    }

                    @Override
                    public void onComplete() {}
                });
    }

    public void reqSessionResult(final SessionInfoMo sessionInfoMo) {
        mldReqSessionResultLoadState.setValue(new SessionResultLoadState(LoadState.Loading, sessionInfoMo));
        dataRepository.getSessionResult(new SessionResultReq(sessionInfoMo.sessionId, sessionInfoMo.sessionSecretKey))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(this)
                .subscribe(new DisposableObserver<BaseResp<LoginResp>>() {
                    @Override
                    public void onNext(@NonNull BaseResp<LoginResp> response) {
                        if(BaseConstants.API_HANDLE_SUCCESS == response.code){
                            if(response.data != null && response.data.token != null && !response.data.token.isEmpty()){
                                mldReqSessionResultLoadState.setValue(new SessionResultLoadState(LoadState.Success, sessionInfoMo, response.data.token));
                            }else{
                                mldReqSessionResultLoadState.setValue(new SessionResultLoadState(LoadState.Failed, sessionInfoMo));
                            }
                        }else if(10011 == response.code || 10012 == response.code){
                            mldReqSessionResultLoadState.setValue(new SessionResultLoadState(LoadState.Failed, sessionInfoMo, response.code));
                        }else{
                            mldToastMsg.setValue(response.message);
                            mldReqSessionResultLoadState.setValue(new SessionResultLoadState(LoadState.Failed, sessionInfoMo));
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        mldReqSessionResultLoadState.setValue(new SessionResultLoadState(LoadState.Failed, sessionInfoMo));
                    }

                    @Override
                    public void onComplete() {}
                });
    }

}
