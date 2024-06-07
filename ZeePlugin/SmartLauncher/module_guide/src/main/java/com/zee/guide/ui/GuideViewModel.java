package com.zee.guide.ui;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.zee.guide.data.GuideRepository;
import com.zeewain.base.data.protocol.request.UpgradeReq;
import com.zeewain.base.data.protocol.response.DeviceInfoResp;
import com.zee.guide.data.protocol.response.ServicePkgInfoResp;
import com.zeewain.base.config.BaseConstants;
import com.zeewain.base.data.protocol.response.BaseResp;
import com.zeewain.base.data.protocol.response.UpgradeResp;
import com.zeewain.base.model.DataLoadState;
import com.zeewain.base.model.LoadState;
import com.zeewain.base.ui.BaseViewModel;
import com.zeewain.base.utils.CommonUtils;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

public class GuideViewModel extends BaseViewModel {
    private final GuideRepository guideRepository;
    public MutableLiveData<LoadState> mldDeviceInfoLoadState = new MutableLiveData<>();
    public MutableLiveData<LoadState> mldServicePackInfoLoadState = new MutableLiveData<>();
    public MutableLiveData<String> mldToastMsg = new MutableLiveData<>();
    public MutableLiveData<Long> mldWifiListUpdate = new MutableLiveData<>();
    public DeviceInfoResp deviceInfoResp;
    public ServicePkgInfoResp servicePkgInfoResp;
    public MutableLiveData<LoadState> mldHostAppUpgradeState = new MutableLiveData<>();
    public MutableLiveData<LoadState> mldManagerAppUpgradeState = new MutableLiveData<>();
    public UpgradeResp hostAppUpgradeResp;
    public UpgradeResp managerAppUpgradeResp;
    public boolean isUserDiscardUpgrade = false;

    public GuideViewModel(GuideRepository guideRepository) {
        this.guideRepository = guideRepository;
        CommonUtils.setPersistData();
    }

    public void reqManagerAppUpgrade(String version) {
        mldManagerAppUpgradeState.setValue(LoadState.Loading);
        UpgradeReq upgradeReq = new UpgradeReq(version, BaseConstants.MANAGER_APP_SOFTWARE_CODE);
        guideRepository.getUpgradeVersionInfo(upgradeReq)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(this)
                .subscribe(new DisposableObserver<BaseResp<UpgradeResp>>() {
                    @Override
                    public void onNext(@NonNull BaseResp<UpgradeResp> response) {
                        if(BaseConstants.API_HANDLE_SUCCESS == response.code) {
                            managerAppUpgradeResp = response.data;
                            if (managerAppUpgradeResp != null) {
                                if (managerAppUpgradeResp.getVersionId() == null || managerAppUpgradeResp.getVersionId().isEmpty()) {
                                    managerAppUpgradeResp = null;
                                }
                            }
                            mldManagerAppUpgradeState.setValue(LoadState.Success);
                        }else{
                            mldToastMsg.setValue(response.message);
                            mldManagerAppUpgradeState.setValue(LoadState.Failed);
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        mldToastMsg.setValue("网络异常，请检查网络设置！");
                        mldManagerAppUpgradeState.setValue(LoadState.Failed);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    public void reqHostAppUpgrade(String version) {
        mldHostAppUpgradeState.setValue(LoadState.Loading);
        UpgradeReq upgradeReq = new UpgradeReq(version, BaseConstants.HOST_APP_SOFTWARE_CODE);
        guideRepository.getUpgradeVersionInfo(upgradeReq)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(this)
                .subscribe(new DisposableObserver<BaseResp<UpgradeResp>>() {
                    @Override
                    public void onNext(@NonNull BaseResp<UpgradeResp> response) {
                        if(BaseConstants.API_HANDLE_SUCCESS == response.code) {
                            hostAppUpgradeResp = response.data;
                            if (hostAppUpgradeResp != null) {
                                if (hostAppUpgradeResp.getVersionId() == null || hostAppUpgradeResp.getVersionId().isEmpty()) {
                                    hostAppUpgradeResp = null;
                                }
                            }
                            mldHostAppUpgradeState.setValue(LoadState.Success);
                        }else{
                            mldToastMsg.setValue(response.message);
                            mldHostAppUpgradeState.setValue(LoadState.Failed);
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        mldToastMsg.setValue("网络异常，请检查网络设置！");
                        mldHostAppUpgradeState.setValue(LoadState.Failed);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    public void reqDeviceInfo(String deviceSn) {
        mldDeviceInfoLoadState.setValue(LoadState.Loading);
        guideRepository.getDeviceInfo(deviceSn)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(this)
                .subscribe(new DisposableObserver<BaseResp<DeviceInfoResp>>() {
                    @Override
                    public void onNext(@NonNull BaseResp<DeviceInfoResp> resp) {
                        if (resp.code == BaseConstants.API_HANDLE_SUCCESS) {
                            deviceInfoResp = resp.data;
                            mldDeviceInfoLoadState.setValue(LoadState.Success);
                        } else {
                            mldDeviceInfoLoadState.setValue(LoadState.Failed);
                            mldToastMsg.setValue(resp.message);
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        mldToastMsg.setValue("网络异常，请检查网络设置！");
                        mldDeviceInfoLoadState.setValue(LoadState.Failed);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    public void reqServicePackInfo(){
        mldServicePackInfoLoadState.setValue(LoadState.Loading);
        guideRepository.getServicePackInfo(CommonUtils.getDeviceSn())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(this)
                .subscribe(new DisposableObserver<BaseResp<ServicePkgInfoResp>>() {
                    @Override
                    public void onNext(@NonNull BaseResp<ServicePkgInfoResp> response) {
                        if(response.code == BaseConstants.API_HANDLE_SUCCESS){
                            servicePkgInfoResp = response.data;
                            mldServicePackInfoLoadState.setValue(LoadState.Success);
                        }else{
                            mldToastMsg.setValue(response.message);
                            mldServicePackInfoLoadState.setValue(LoadState.Failed);
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        mldToastMsg.setValue("网络异常，请检查网络设置！");
                        mldServicePackInfoLoadState.setValue(LoadState.Failed);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    public boolean useTopicLogin(){
        if(servicePkgInfoResp != null && servicePkgInfoResp.extendJson != null){
            return servicePkgInfoResp.extendJson.loginMode == 2;
        }
        return false;
    }

    public boolean isAiGestureEnable(){
        if(servicePkgInfoResp != null && servicePkgInfoResp.extendJson != null){
            if(servicePkgInfoResp.extendJson.aiGesture != null)
                return servicePkgInfoResp.extendJson.aiGesture.enabled;
        }
        return true;
    }
}
