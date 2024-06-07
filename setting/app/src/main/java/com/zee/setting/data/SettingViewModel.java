package com.zee.setting.data;


import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.zee.setting.base.BaseConstants;
import com.zee.setting.base.BaseViewModel;
import com.zee.setting.base.LoadState;
import com.zee.setting.base.data.protocol.request.PublishReq;
import com.zee.setting.base.data.protocol.response.BaseResp;
import com.zee.setting.base.data.protocol.response.PublishResp;
import com.zee.setting.bean.GuideInfo;
import com.zee.setting.data.protocol.request.UpgradeReq;
import com.zee.setting.data.protocol.response.AgreementResp;
import com.zee.setting.data.protocol.response.UpgradeResp;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;


public class SettingViewModel extends BaseViewModel {

    private static final String TAG = "SettingViewModel";

    private final SettingRepository dataRepository;
    public MutableLiveData<LoadState> mPublishState = new MutableLiveData<>();
    public MutableLiveData<LoadState> mUpgradeState = new MutableLiveData<>();
    public MutableLiveData<LoadState> mAgreementState = new MutableLiveData<>();
    public MutableLiveData<GuideInfo> guideInfo = new MutableLiveData<>();
    public UpgradeResp upgradeResp;
    public AgreementResp agreementResp;
    public PublishResp publishResp;

    public SettingViewModel(SettingRepository dataRepository) {
        this.dataRepository = dataRepository;
    }

    public void getPublishVersionInfo(String code) {
        mPublishState.setValue(LoadState.Loading);
        dataRepository.getPublishedVersionInfo(new PublishReq(code))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableObserver<BaseResp<PublishResp>>() {
                    @Override
                    public void onNext(BaseResp<PublishResp> response) {
                        if (response.code == BaseConstants.API_HANDLE_SUCCESS) {
                            publishResp = response.data;
                            mPublishState.setValue(LoadState.Success);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        mPublishState.setValue(LoadState.Failed);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    public void getUpgradeVersionInfo(UpgradeReq upgradeReq) {
        mUpgradeState.setValue(LoadState.Loading);
        dataRepository.getUpgradeVersionInfo(upgradeReq)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(this)
                .subscribe(new DisposableObserver<BaseResp<UpgradeResp>>() {
                    @Override
                    public void onNext(@NonNull BaseResp<UpgradeResp> response) {
                        upgradeResp = response.data;
                        if(upgradeResp != null){
                            if(upgradeResp.getVersionId() == null || upgradeResp.getVersionId().isEmpty()){
                                upgradeResp = null;
                            }
                        }
                        mUpgradeState.setValue(LoadState.Success);
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        mUpgradeState.setValue(LoadState.Failed);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }


    public void getAgreementInfo(String agreementReq) {
        Log.i("ssshhh","agreementReq="+agreementReq.toString());
        mAgreementState.setValue(LoadState.Loading);
        dataRepository.getAgreementInfo(agreementReq)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(this)
                .subscribe(new DisposableObserver<BaseResp<AgreementResp>>() {
                    @Override
                    public void onNext(@NonNull BaseResp<AgreementResp> response) {
                        agreementResp = response.data;
                        mAgreementState.setValue(LoadState.Success);
//                        Log.i("ssshhh","agreementResp="+agreementResp.toString());
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        mAgreementState.setValue(LoadState.Failed);
                        Log.i("ssshhh","ex="+e.getLocalizedMessage());
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

}