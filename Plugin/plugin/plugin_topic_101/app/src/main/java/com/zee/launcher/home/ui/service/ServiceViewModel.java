package com.zee.launcher.home.ui.service;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.zee.launcher.home.data.DataRepository;
import com.zee.launcher.home.data.model.ProductListMo;
import com.zee.launcher.home.data.protocol.request.ProductListBySkuIdsReq;
import com.zeewain.base.data.protocol.response.BaseResp;
import com.zeewain.base.model.LoadState;
import com.zeewain.base.ui.BaseViewModel;

import java.util.Collections;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

public class ServiceViewModel extends BaseViewModel {
    public MutableLiveData<List<ProductListMo.Record>> productRecodeList = new MutableLiveData<>();
    public MutableLiveData<LoadState> mldProductRecodeListLoadState = new MutableLiveData<>();

    public void reqProductListBySkuIds(List<String> skuIdList) {
        mldProductRecodeListLoadState.setValue(LoadState.Loading);
        DataRepository.getInstance().getProductListBySkuIds(new ProductListBySkuIdsReq(skuIdList))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(ServiceViewModel.this)
                .subscribe(new DisposableObserver<BaseResp<List<ProductListMo.Record>>>() {
                    @Override
                    public void onNext(@NonNull BaseResp<List<ProductListMo.Record>> response) {
                        if (response.data.size() > 0) {
                            productRecodeList.setValue(response.data);
                            mldProductRecodeListLoadState.setValue(LoadState.Success);
                        } else mldProductRecodeListLoadState.setValue(LoadState.Failed);
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        mldProductRecodeListLoadState.setValue(LoadState.Failed);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }
}
