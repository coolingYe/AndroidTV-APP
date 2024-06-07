package com.zee.launcher.home.ui.direct;


import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.zee.launcher.home.data.DataRepository;
import com.zee.launcher.home.data.protocol.request.CollectReq;
import com.zee.launcher.home.data.protocol.request.ProDetailReq;
import com.zee.launcher.home.data.protocol.request.PublishReq;
import com.zee.launcher.home.data.protocol.request.RemoveCollectReq;
import com.zee.launcher.home.data.protocol.request.UpgradeReq;
import com.zee.launcher.home.data.protocol.response.CollectResp;
import com.zee.launcher.home.data.protocol.response.FavoriteStateResp;
import com.zee.launcher.home.data.protocol.response.ProDetailResp;
import com.zee.launcher.home.data.protocol.response.PublishResp;
import com.zee.launcher.home.data.protocol.response.UpgradeResp;
import com.zeewain.base.config.BaseConstants;
import com.zeewain.base.data.protocol.response.BaseResp;
import com.zeewain.base.model.DataLoadState;
import com.zeewain.base.model.LoadState;
import com.zeewain.base.ui.BaseViewModel;

import java.util.concurrent.atomic.AtomicInteger;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

public class DirectLoadViewModel extends BaseViewModel {

    private final DataRepository dataRepository;

    public MutableLiveData<LoadState> mldInitLoadState = new MutableLiveData<>();
    private final MutableLiveData<LoadState> mldDetailLoadState = new MutableLiveData<>();
    public MutableLiveData<LoadState> mPublishState = new MutableLiveData<>();
    public MutableLiveData<LoadState> mUpgradeState = new MutableLiveData<>();
    public MutableLiveData<DataLoadState<Boolean>> mCollectListState = new MutableLiveData<>();
    public MutableLiveData<LoadState> mAddCollectState = new MutableLiveData<>();
    public MutableLiveData<LoadState> mRemoveCollectState = new MutableLiveData<>();
    public ProDetailResp proDetailResp;
    public PublishResp publishResp;
    public UpgradeResp upgradeResp;
    private final AtomicInteger pendingPrepareCount = new AtomicInteger();

    public DirectLoadViewModel(DataRepository dataRepository) {
        this.dataRepository = dataRepository;
    }

    public void initDataReq(String skuId){
        proDetailResp = null;
        publishResp = null;
        upgradeResp = null;
        mldInitLoadState.setValue(LoadState.Loading);
        pendingPrepareCount.set(1);
        reqProDetailInfo(skuId);
    }

    private void decrementCountAndCheck(){
        int newPendingCount = pendingPrepareCount.decrementAndGet();
        if(newPendingCount <= 0){
            if(LoadState.Success == mldDetailLoadState.getValue()){
                mldInitLoadState.setValue(LoadState.Success);
            }else{
                mldInitLoadState.setValue(LoadState.Failed);
            }
        }
    }

    public void reqProDetailInfo(String skuId) {
        mldDetailLoadState.setValue(LoadState.Loading);
        dataRepository.getProDetailInfo(new ProDetailReq(skuId))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(this)
                .subscribe(new DisposableObserver<BaseResp<ProDetailResp>>() {
                    @Override
                    public void onNext(@NonNull BaseResp<ProDetailResp> response) {
                        if(response.code == BaseConstants.API_HANDLE_SUCCESS){
                            proDetailResp = response.data;
                            mldDetailLoadState.setValue(LoadState.Success);
                        }else{
                            mldDetailLoadState.setValue(LoadState.Failed);
                        }
                        decrementCountAndCheck();
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        mldDetailLoadState.setValue(LoadState.Failed);
                        decrementCountAndCheck();
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    public void getPublishVersionInfo(PublishReq publishReq) {
        mPublishState.setValue(LoadState.Loading);
        dataRepository.getPublishedVersionInfo(publishReq)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(this)
                .subscribe(new DisposableObserver<BaseResp<PublishResp>>() {
                    @Override
                    public void onNext(@NonNull BaseResp<PublishResp> response) {
                        publishResp = response.data;
                        mPublishState.setValue(LoadState.Success);
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

    public void reqFavoriteState(final String skuId) {
        dataRepository.getFavoriteState(skuId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(this)
                .subscribe(new DisposableObserver<BaseResp<FavoriteStateResp>>() {
                    @Override
                    public void onNext(@NonNull BaseResp<FavoriteStateResp> resp) {
                        if (resp.code == BaseConstants.API_HANDLE_SUCCESS) {
                            if(resp.data != null && resp.data.getObjId() != null){
                                mCollectListState.setValue(new DataLoadState<>(LoadState.Success, true));
                            }else{
                                mCollectListState.setValue(new DataLoadState<>(LoadState.Success, false));
                            }
                        } else {
                            mCollectListState.setValue(new DataLoadState<>(LoadState.Failed));
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        mCollectListState.setValue(new DataLoadState<>(LoadState.Failed));
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    public void addFavorites(CollectReq collectReq) {
        mAddCollectState.setValue(LoadState.Loading);
        dataRepository.addFavorites(collectReq)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(this)
                .subscribe(new DisposableObserver<BaseResp<CollectResp>>() {
                    @Override
                    public void onNext(@NonNull BaseResp<CollectResp> response) {
                        if(response.code == BaseConstants.API_HANDLE_SUCCESS){
                            mAddCollectState.setValue(LoadState.Success);
                        } else{
                            mAddCollectState.setValue(LoadState.Failed);
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        mAddCollectState.setValue(LoadState.Failed);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    public void removeFavorites(RemoveCollectReq removeCollectReq) {
        mRemoveCollectState.setValue(LoadState.Loading);
        dataRepository.removeFavorites(removeCollectReq)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(this)
                .subscribe(new DisposableObserver<BaseResp<String>>() {
                    @Override
                    public void onNext(@NonNull BaseResp<String> response) {
                        mRemoveCollectState.setValue(LoadState.Success);
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        mRemoveCollectState.setValue(LoadState.Failed);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }
}