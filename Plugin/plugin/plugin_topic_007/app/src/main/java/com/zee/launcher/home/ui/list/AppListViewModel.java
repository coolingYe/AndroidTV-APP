package com.zee.launcher.home.ui.list;


import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.zee.launcher.home.data.DataRepository;
import com.zee.launcher.home.data.model.ProductListMo;
import com.zee.launcher.home.data.protocol.request.AppListReq;
import com.zee.launcher.home.data.protocol.request.ProductListBySkuIdsReq;
import com.zeewain.base.config.BaseConstants;
import com.zeewain.base.data.protocol.response.BaseResp;
import com.zeewain.base.model.LoadState;
import com.zeewain.base.ui.BaseViewModel;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

public class AppListViewModel extends BaseViewModel {

    private final DataRepository dataRepository;

    public MutableLiveData<LoadState> mldInitLoadState = new MutableLiveData<>();
    public MutableLiveData<String> mldToastMsg = new MutableLiveData<>();
    public List<ProductListMo.Record> appRequestList;

    public AppListViewModel(DataRepository dataRepository) {
        this.dataRepository = dataRepository;
    }

    public void reqAppList(final String deviceSn, String softwareCode) {
        mldInitLoadState.setValue(LoadState.Loading);
        dataRepository.getAppList(new AppListReq(deviceSn, softwareCode))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(this)
                .subscribe(new DisposableObserver<BaseResp<List<ProductListMo.Record>>>() {
                    @Override
                    public void onNext(@NonNull BaseResp<List<ProductListMo.Record>> response) {

                            if (response.code == BaseConstants.API_HANDLE_SUCCESS) {
                                appRequestList = response.data;
                                mldInitLoadState.setValue(LoadState.Success);
                            } else {
                                mldToastMsg.setValue(response.message);
                                mldInitLoadState.setValue(LoadState.Failed);
                            }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                            mldInitLoadState.setValue(LoadState.Failed);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    public void reqTestAppList(List<String> skuidList,String deviceSn ) {
        mldInitLoadState.setValue(LoadState.Loading);
        dataRepository.getProductListBySkuIds(new ProductListBySkuIdsReq(skuidList,deviceSn))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(this)
                .subscribe(new DisposableObserver<BaseResp<List<ProductListMo.Record>>>() {
                    @Override
                    public void onNext(@NonNull BaseResp<List<ProductListMo.Record>> response) {
                        if (response.code == BaseConstants.API_HANDLE_SUCCESS) {
                            appRequestList = response.data;
                            mldInitLoadState.setValue(LoadState.Success);
                        } else {
                            mldToastMsg.setValue(response.message);
                            mldInitLoadState.setValue(LoadState.Failed);
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        mldInitLoadState.setValue(LoadState.Failed);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }
}