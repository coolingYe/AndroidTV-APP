package com.zwn.launcher.ui.upgrade;


import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import com.zeewain.base.config.BaseConstants;
import com.zeewain.base.data.protocol.response.BaseResp;
import com.zeewain.base.model.LoadState;
import com.zeewain.base.ui.BaseViewModel;
import com.zwn.launcher.data.DataRepository;
import com.zeewain.base.data.protocol.request.UpgradeReq;
import com.zeewain.base.data.protocol.response.UpgradeResp;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;


public class UpgradeTipViewModel extends BaseViewModel {

    private final DataRepository dataRepository;
    public MutableLiveData<LoadState> mldHostAppUpgradeState = new MutableLiveData<>();
    public UpgradeResp hostAppUpgradeResp;

    public UpgradeTipViewModel(DataRepository dataRepository) {
        this.dataRepository = dataRepository;
    }

    public void reqHostAppUpgrade(String version) {
        mldHostAppUpgradeState.setValue(LoadState.Loading);
        UpgradeReq upgradeReq = new UpgradeReq(version, BaseConstants.HOST_APP_SOFTWARE_CODE);
        dataRepository.getUpgradeVersionInfo(upgradeReq)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(this)
                .subscribe(new DisposableObserver<BaseResp<UpgradeResp>>() {
                    @Override
                    public void onNext(@NonNull BaseResp<UpgradeResp> response) {
                        hostAppUpgradeResp = response.data;
                        if(hostAppUpgradeResp != null){
                            if(hostAppUpgradeResp.getVersionId() == null || hostAppUpgradeResp.getVersionId().isEmpty()){
                                hostAppUpgradeResp = null;
                            }
                        }
                        mldHostAppUpgradeState.setValue(LoadState.Success);
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        mldHostAppUpgradeState.setValue(LoadState.Failed);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

}
