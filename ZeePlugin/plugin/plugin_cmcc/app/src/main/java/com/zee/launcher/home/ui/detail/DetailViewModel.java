package com.zee.launcher.home.ui.detail;


import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.zee.launcher.home.data.DataRepository;
import com.zee.launcher.home.data.model.ProductListMo;
import com.zee.launcher.home.data.protocol.request.CollectReq;
import com.zee.launcher.home.data.protocol.request.ProDetailReq;
import com.zee.launcher.home.data.protocol.request.ProductRecommendReq;
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
import com.zeewain.base.model.LoadState;
import com.zeewain.base.ui.BaseViewModel;
import com.zeewain.base.model.DataLoadState;
import com.zeewain.base.utils.CommonUtils;


import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

public class DetailViewModel extends BaseViewModel {

    private final DataRepository dataRepository;

    public MutableLiveData<LoadState> mldInitLoadState = new MutableLiveData<>();
    private final MutableLiveData<LoadState> mldDetailLoadState = new MutableLiveData<>();
    private final MutableLiveData<LoadState> mldProductRecommendState = new MutableLiveData<>();
    public MutableLiveData<LoadState> mPublishState = new MutableLiveData<>();
    public MutableLiveData<LoadState> mUpgradeState = new MutableLiveData<>();
    public MutableLiveData<DataLoadState<Boolean>> mCollectListState = new MutableLiveData<>();
    public MutableLiveData<LoadState> mAddCollectState = new MutableLiveData<>();
    public MutableLiveData<LoadState> mRemoveCollectState = new MutableLiveData<>();
    public MutableLiveData<String> mldToastMsg = new MutableLiveData<>();
    public MutableLiveData<Integer> mldSpecialRespCode = new MutableLiveData<>();
    public ProDetailResp proDetailResp;
    public PublishResp publishResp;
    public UpgradeResp upgradeResp;
    public boolean isFromCacheUpgradeVersionInfo = false;
    public List<ProductListMo.Record> productRecommendList;
    private final AtomicInteger pendingPrepareCount = new AtomicInteger();
    private String usedSkuId;

    public DetailViewModel(DataRepository dataRepository) {
        this.dataRepository = dataRepository;
    }

    public void initDataReq(String skuId){
        usedSkuId = skuId;
        proDetailResp = null;
        publishResp = null;
        upgradeResp = null;
        productRecommendList = null;
        mldInitLoadState.setValue(LoadState.Loading);
        pendingPrepareCount.set(2);
        reqProDetailInfo(skuId);
        reqProductRecommendList(skuId, 7);
    }

    private void decrementCountAndCheck(){
        int newPendingCount = pendingPrepareCount.decrementAndGet();
        if(newPendingCount <= 0){
            if(LoadState.Success == mldDetailLoadState.getValue() && LoadState.Success == mldProductRecommendState.getValue()){
                mldInitLoadState.setValue(LoadState.Success);
            }else{
                mldInitLoadState.setValue(LoadState.Failed);
            }
        }
    }

    public void reqProDetailInfo(final String skuId) {
        if(skuId.equals(usedSkuId)) {
            mldDetailLoadState.setValue(LoadState.Loading);
        }
        dataRepository.getProDetailInfo(new ProDetailReq(skuId, CommonUtils.getDeviceSn()))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(this)
                .subscribe(new DisposableObserver<BaseResp<ProDetailResp>>() {
                    @Override
                    public void onNext(@NonNull BaseResp<ProDetailResp> response) {
                        if(skuId.equals(usedSkuId)) {
                            proDetailResp = response.data;
                            if (response.code == BaseConstants.API_HANDLE_SUCCESS) {
                                if (proDetailResp != null && proDetailResp.getSkuId() != null) {
                                    mldDetailLoadState.setValue(LoadState.Success);
                                } else {
                                    mldToastMsg.setValue("数据配置错误！");
                                    mldDetailLoadState.setValue(LoadState.Failed);
                                }
                            } else {
                                mldToastMsg.setValue(response.message);
                                if (response.code == BaseConstants.SpecialRespCode.FINISH_PAGE) {
                                    mldSpecialRespCode.setValue(BaseConstants.SpecialRespCode.FINISH_PAGE);
                                    return;
                                }
                                mldDetailLoadState.setValue(LoadState.Failed);
                            }
                            decrementCountAndCheck();
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        if(skuId.equals(usedSkuId)) {
                            mldDetailLoadState.setValue(LoadState.Failed);
                            decrementCountAndCheck();
                        }
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    public void reqProductRecommendList(final String skuId, int topN) {
        if(skuId.equals(usedSkuId)) {
            mldProductRecommendState.setValue(LoadState.Loading);
        }
        dataRepository.getProductRecommend(new ProductRecommendReq(skuId, CommonUtils.getDeviceSn(), topN))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(this)
                .subscribe(new DisposableObserver<BaseResp<List<ProductListMo.Record>>>() {
                    @Override
                    public void onNext(@NonNull BaseResp<List<ProductListMo.Record>> response) {
                        if(skuId.equals(usedSkuId)) {
                            if (response.code == BaseConstants.API_HANDLE_SUCCESS) {
                                productRecommendList = response.data;
                                mldProductRecommendState.setValue(LoadState.Success);
                            } else {
                                mldToastMsg.setValue(response.message);
                                mldProductRecommendState.setValue(LoadState.Failed);
                            }
                            decrementCountAndCheck();
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        if(skuId.equals(usedSkuId)) {
                            mldProductRecommendState.setValue(LoadState.Failed);
                            decrementCountAndCheck();
                        }
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    public void getPublishVersionInfo(final String skuId, PublishReq publishReq) {
        if (skuId.equals(usedSkuId)){
            mPublishState.setValue(LoadState.Loading);
        }
        dataRepository.getPublishedVersionInfo(publishReq)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(this)
                .subscribe(new DisposableObserver<BaseResp<PublishResp>>() {
                    @Override
                    public void onNext(@NonNull BaseResp<PublishResp> response) {
                        if(skuId.equals(usedSkuId)) {
                            if (response.code == BaseConstants.API_HANDLE_SUCCESS) {
                                publishResp = response.data;
                                mPublishState.setValue(LoadState.Success);
                            } else {
                                mldToastMsg.setValue(response.message);
                                mPublishState.setValue(LoadState.Failed);
                            }
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        if (skuId.equals(usedSkuId)) {
                            mPublishState.setValue(LoadState.Failed);
                        }
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    public void getUpgradeVersionInfo(final String skuId, UpgradeReq upgradeReq) {
        if (skuId.equals(usedSkuId)) {
            mUpgradeState.setValue(LoadState.Loading);
        }
        dataRepository.getUpgradeVersionInfo(upgradeReq)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(this)
                .subscribe(new DisposableObserver<BaseResp<UpgradeResp>>() {
                    @Override
                    public void onNext(@NonNull BaseResp<UpgradeResp> response) {
                        if (skuId.equals(usedSkuId)) {
                            isFromCacheUpgradeVersionInfo = response.isCache;
                            if (response.code == BaseConstants.API_HANDLE_SUCCESS) {
                                upgradeResp = response.data;
                                if (upgradeResp != null) {
                                    if (upgradeResp.getVersionId() == null || upgradeResp.getVersionId().isEmpty()) {
                                        upgradeResp = null;
                                    }
                                }
                                mUpgradeState.setValue(LoadState.Success);
                            } else {
                                mldToastMsg.setValue(response.message);
                                mUpgradeState.setValue(LoadState.Failed);
                            }
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        if (skuId.equals(usedSkuId)) {
                            mUpgradeState.setValue(LoadState.Failed);
                        }
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
                        if (skuId.equals(usedSkuId)) {
                            if (resp.code == BaseConstants.API_HANDLE_SUCCESS) {
                                if (resp.data != null && resp.data.getObjId() != null) {
                                    mCollectListState.setValue(new DataLoadState<>(LoadState.Success, true));
                                } else {
                                    mCollectListState.setValue(new DataLoadState<>(LoadState.Success, false));
                                }
                            } else {
                                mldToastMsg.setValue(resp.message);
                                mCollectListState.setValue(new DataLoadState<>(LoadState.Failed));
                            }
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        if (skuId.equals(usedSkuId)) {
                            mCollectListState.setValue(new DataLoadState<>(LoadState.Failed));
                        }
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
                        } else {
                            mldToastMsg.setValue(response.message);
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
                        if(response.code == BaseConstants.API_HANDLE_SUCCESS) {
                            mRemoveCollectState.setValue(LoadState.Success);
                        } else {
                            mldToastMsg.setValue(response.message);
                            mRemoveCollectState.setValue(LoadState.Failed);
                        }
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