package com.zee.launcher.home.ui.direct;


import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.zee.launcher.home.data.DataRepository;
import com.zee.launcher.home.data.protocol.request.CoursewareStartReq;
import com.zee.launcher.home.data.protocol.request.ProDetailReq;
import com.zee.launcher.home.data.protocol.request.PublishReq;
import com.zee.launcher.home.data.protocol.request.UpgradeReq;
import com.zee.launcher.home.data.protocol.response.ProDetailResp;
import com.zee.launcher.home.data.protocol.response.PublishResp;
import com.zee.launcher.home.data.protocol.response.UpgradeResp;
import com.zeewain.base.config.BaseConstants;
import com.zeewain.base.data.protocol.response.BaseResp;
import com.zeewain.base.model.DataLoadState;
import com.zeewain.base.model.LoadState;
import com.zeewain.base.ui.BaseViewModel;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

public class DirectLoadViewModel extends BaseViewModel {

    private final DataRepository dataRepository;

    public MutableLiveData<LoadState> mldDetailLoadState = new MutableLiveData<>();
    public MutableLiveData<LoadState> mPublishState = new MutableLiveData<>();
    public MutableLiveData<DataLoadState<UpgradeReq>> mUpgradeState = new MutableLiveData<>();
    public MutableLiveData<String> mldToastMsg = new MutableLiveData<>();
    public ProDetailResp proDetailResp;
    public PublishResp publishResp;
    public UpgradeResp upgradeResp;
    public int tryReqProDetailInfoTimes = 3;
    public int tryReqPublishVersionInfoTimes = 3;
    public int tryReqUpgradeVersionInfoTimes = 3;

    public DirectLoadViewModel(DataRepository dataRepository) {
        this.dataRepository = dataRepository;
    }

    public void reqProDetailInfo(String skuId) {
        tryReqProDetailInfoTimes --;
        mldDetailLoadState.setValue(LoadState.Loading);
        dataRepository.getProDetailInfo(new ProDetailReq(skuId))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(this)
                .subscribe(new DisposableObserver<BaseResp<ProDetailResp>>() {
                    @Override
                    public void onNext(@NonNull BaseResp<ProDetailResp> response) {
                        if(response.code == BaseConstants.API_HANDLE_SUCCESS){
                            if(response.data != null && response.data.getSkuId() != null){
                                proDetailResp = response.data;
                                mldDetailLoadState.setValue(LoadState.Success);
                            }else{
                                if(tryReqProDetailInfoTimes == 0)
                                    mldToastMsg.setValue("详情信息错误！");
                                mldDetailLoadState.setValue(LoadState.Failed);
                            }
                        }else{
                            if(tryReqProDetailInfoTimes == 0)
                                mldToastMsg.setValue(response.message);
                            mldDetailLoadState.setValue(LoadState.Failed);
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        mldDetailLoadState.setValue(LoadState.Failed);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    public void getPublishVersionInfo(PublishReq publishReq) {
        tryReqPublishVersionInfoTimes --;
        mPublishState.setValue(LoadState.Loading);
        dataRepository.getPublishedVersionInfo(publishReq)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(this)
                .subscribe(new DisposableObserver<BaseResp<PublishResp>>() {
                    @Override
                    public void onNext(@NonNull BaseResp<PublishResp> response) {
                        if(response.code == BaseConstants.API_HANDLE_SUCCESS) {
                            publishResp = response.data;
                            mPublishState.setValue(LoadState.Success);
                        }else{
                            if(tryReqPublishVersionInfoTimes == 0)
                                mldToastMsg.setValue(response.message);
                            mPublishState.setValue(LoadState.Failed);
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        mPublishState.setValue(LoadState.Failed);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    public void getUpgradeVersionInfo(final UpgradeReq upgradeReq) {
        tryReqUpgradeVersionInfoTimes --;
        mUpgradeState.setValue(new DataLoadState<>(LoadState.Loading, upgradeReq));
        dataRepository.getUpgradeVersionInfo(upgradeReq)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(this)
                .subscribe(new DisposableObserver<BaseResp<UpgradeResp>>() {
                    @Override
                    public void onNext(@NonNull BaseResp<UpgradeResp> response) {
                        if (response.code == BaseConstants.API_HANDLE_SUCCESS) {
                            upgradeResp = response.data;
                            if(upgradeResp != null){
                                if(upgradeResp.getVersionId() == null || upgradeResp.getVersionId().isEmpty()){
                                    upgradeResp = null;
                                }
                            }
                            mUpgradeState.setValue(new DataLoadState<>(LoadState.Success, upgradeReq));
                        }else{
                            if(tryReqUpgradeVersionInfoTimes == 0)
                                mldToastMsg.setValue(response.message);
                            mUpgradeState.setValue(new DataLoadState<>(LoadState.Failed, upgradeReq));
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        mUpgradeState.setValue(new DataLoadState<>(LoadState.Failed, upgradeReq));
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    public void reqStartCourseware(){
        if(proDetailResp != null){
            CoursewareStartReq coursewareStartReq = new CoursewareStartReq(proDetailResp.getSkuId(), proDetailResp.getProductTitle(), proDetailResp.getUseImgUrl());
            dataRepository.startCourseware(coursewareStartReq)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnSubscribe(this)
                    .subscribe(new DisposableObserver<BaseResp<String>>() {
                        @Override
                        public void onNext(@NonNull BaseResp<String> response) {
                        }

                        @Override
                        public void onError(@NonNull Throwable e) {
                        }

                        @Override
                        public void onComplete() {
                        }
                    });
        }

    }

}