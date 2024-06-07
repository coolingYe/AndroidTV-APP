package com.zee.launcher.home.ui.cache;

import android.os.CountDownTimer;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.zee.launcher.home.config.ProdConstants;
import com.zee.launcher.home.data.DataRepository;
import com.zee.launcher.home.data.model.ProductListMo;
import com.zee.launcher.home.data.protocol.request.ProductListBySkuIdsReq;
import com.zeewain.base.config.BaseConstants;
import com.zeewain.base.data.protocol.response.BaseResp;
import com.zeewain.base.model.DataLoadState;
import com.zeewain.base.model.LoadState;
import com.zeewain.base.ui.BaseViewModel;
import com.zeewain.base.utils.CommonUtils;
import com.zwn.lib_download.db.CareController;
import com.zwn.lib_download.model.DownloadInfo;
import com.zwn.user.data.UserRepository;
import com.zwn.user.data.protocol.request.FavoritePagedReq;
import com.zwn.user.data.protocol.response.FavoritePagedResp;
import com.zwn.user.data.protocol.response.HistoryResp;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;


public class CacheManagerViewModel extends BaseViewModel {
    public MutableLiveData<String> mldToastMsg = new MutableLiveData<>();
    public MutableLiveData<LoadState> mldUserDataLoadState = new MutableLiveData<>();
    public MutableLiveData<DataLoadState<String>> mldProductRecodeListLoadState = new MutableLiveData<>();
    public MutableLiveData<List<DownloadInfo>> mldDownloadInfoListUpdate = new MutableLiveData<>();
    private static final ExecutorService mFixedPool = Executors.newFixedThreadPool(1);

    public void reqUserCenterData() {
        mldUserDataLoadState.setValue(LoadState.Loading);
        UserRepository.getInstance().getUserFavorites(new FavoritePagedReq(1, 100))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(this)
                .subscribe(new DisposableObserver<BaseResp<FavoritePagedResp>>() {
                    @Override
                    public void onNext(@NonNull BaseResp<FavoritePagedResp> resp) {
                        if (resp.code == BaseConstants.API_HANDLE_SUCCESS) {
                            if (resp.isCache) {
                                mldToastMsg.setValue("数据获取失败，请检查网络状态");
                                mldUserDataLoadState.setValue(LoadState.Failed);
                            } else {
                                reqUserHistory();
                            }
                        } else {
                            mldToastMsg.setValue(resp.message);
                            mldUserDataLoadState.setValue(LoadState.Failed);
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        mldToastMsg.setValue("数据获取失败，请检查网络状态");
                        mldUserDataLoadState.setValue(LoadState.Failed);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    public void reqUserHistory() {
        mldUserDataLoadState.setValue(LoadState.Loading);
        UserRepository.getInstance().getUserHistory()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(this)
                .subscribe(new DisposableObserver<BaseResp<HistoryResp>>() {
                    @Override
                    public void onNext(@NonNull BaseResp<HistoryResp> resp) {
                        if (resp.code == BaseConstants.API_HANDLE_SUCCESS) {
                            if (resp.isCache) {
                                mldToastMsg.setValue("数据获取失败，请检查网络状态");
                                mldUserDataLoadState.setValue(LoadState.Failed);
                            } else {
                                mldUserDataLoadState.setValue(LoadState.Success);
                            }
                        } else {
                            mldToastMsg.setValue(resp.message);
                            mldUserDataLoadState.setValue(LoadState.Failed);
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        mldToastMsg.setValue("数据获取失败，请检查网络状态");
                        mldUserDataLoadState.setValue(LoadState.Failed);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    public void reqProductListBySkuIds(List<String> skuIdList, final String careKey) {
        if (skuIdList.size() > ProdConstants.PRD_PAGE_SIZE) {
            skuIdList = skuIdList.subList(0, ProdConstants.PRD_PAGE_SIZE);
        }
        mldProductRecodeListLoadState.setValue(new DataLoadState<>(LoadState.Loading));
        DataRepository.getInstance().getProductListBySkuIds(new ProductListBySkuIdsReq(skuIdList, CommonUtils.getDeviceSn()))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(CacheManagerViewModel.this)
                .subscribe(new DisposableObserver<BaseResp<List<ProductListMo.Record>>>() {
                    @Override
                    public void onNext(@NonNull BaseResp<List<ProductListMo.Record>> response) {
                        if (response.code == BaseConstants.API_HANDLE_SUCCESS) {
                            if (response.isCache) {
                                mldToastMsg.setValue("数据获取失败，请检查网络状态");
                                mldProductRecodeListLoadState.setValue(new DataLoadState<>(LoadState.Failed, careKey));
                            } else {
                                mldProductRecodeListLoadState.setValue(new DataLoadState<>(LoadState.Success, careKey));
                            }
                        } else {
                            mldToastMsg.setValue(response.message);
                            mldProductRecodeListLoadState.setValue(new DataLoadState<>(LoadState.Failed, careKey));
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        mldToastMsg.setValue("数据获取失败，请检查网络状态");
                        mldProductRecodeListLoadState.setValue(new DataLoadState<>(LoadState.Failed, careKey));
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    public final CountDownTimer countDownTimer = new CountDownTimer(
            30 * 24 * 60 * 60 * 1000L,
            1000) {
        @Override
        public void onTick(long millisUntilFinished) {
            mFixedPool.execute(() -> {
                getDownloadList();
            });
        }

        @Override
        public void onFinish() {

        }
    };

    public void getDownloadList() {
        List<DownloadInfo> allDownloadInfo = CareController.instance.getAllDownloadInfo(
                "type=" + BaseConstants.DownloadFileType.PLUGIN_APP);
        mldDownloadInfoListUpdate.postValue(allDownloadInfo);
    }

}
