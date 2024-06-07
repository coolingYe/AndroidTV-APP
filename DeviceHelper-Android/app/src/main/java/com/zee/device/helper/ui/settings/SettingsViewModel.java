package com.zee.device.helper.ui.settings;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.zee.device.base.config.BaseConstants;

import com.zee.device.base.ui.BaseViewModel;
import com.zee.device.home.data.DataRepository;
import com.zee.device.home.data.protocol.request.UpgradeReq;
import com.zee.device.home.data.protocol.response.BaseResp;
import com.zee.device.home.data.protocol.response.UpgradeResp;
import com.zee.device.home.ui.home.model.LoadState;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

public class SettingsViewModel extends BaseViewModel {

    public UpgradeResp hostAppUpgradeResp;
    public MutableLiveData<LoadState> mldHostAppUpgradeState = new MutableLiveData<>();
    public MutableLiveData<String> mldToastMsg = new MutableLiveData<>();

    public void reqHostAppUpgrade(String version) {
        mldHostAppUpgradeState.setValue(LoadState.Loading);
        UpgradeReq upgradeReq = new UpgradeReq(version, BaseConstants.ZEE_HELP_APP_SOFTWARE_CODE);
        DataRepository.getInstance().getUpgradeVersionInfo(upgradeReq)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(this)
                .subscribe(new DisposableObserver<BaseResp<UpgradeResp>>() {
                    @Override
                    public void onNext(@NonNull BaseResp<UpgradeResp> response) {
                        if (BaseConstants.API_HANDLE_SUCCESS == response.code) {
                            hostAppUpgradeResp = response.data;
                            if (hostAppUpgradeResp != null) {
                                if (hostAppUpgradeResp.getVersionId() == null || hostAppUpgradeResp.getVersionId().isEmpty()) {
                                    hostAppUpgradeResp = null;
                                }
                            }
                            mldHostAppUpgradeState.setValue(LoadState.Success);
                        } else {
                            mldToastMsg.setValue(response.message);
                            mldHostAppUpgradeState.setValue(LoadState.Failed);
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        mldToastMsg.setValue("网络异常！");
                        mldHostAppUpgradeState.setValue(LoadState.Failed);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

}
