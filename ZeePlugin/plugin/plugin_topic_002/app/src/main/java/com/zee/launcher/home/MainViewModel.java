package com.zee.launcher.home;


import android.annotation.SuppressLint;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.zee.launcher.home.config.ProdConstants;
import com.zee.launcher.home.data.DataRepository;
import com.zee.launcher.home.data.layout.GlobalLayout;
import com.zee.launcher.home.data.model.ProductListMo;
import com.zee.launcher.home.data.protocol.request.ProductListBySkuIdsReq;
import com.zee.launcher.home.data.protocol.response.ServicePkgInfoResp;
import com.zee.launcher.home.utils.GlobalLayoutHelper;
import com.zeewain.base.config.BaseConstants;
import com.zeewain.base.data.protocol.response.BaseResp;
import com.zeewain.base.model.LoadState;
import com.zeewain.base.ui.BaseViewModel;
import com.zeewain.base.utils.CommonUtils;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

public class MainViewModel extends BaseViewModel {
    private final DataRepository dataRepository;

    public GlobalLayout globalLayout;
    public List<ProductListMo.Record> productRecodeList = new ArrayList<>();
    public MutableLiveData<LoadState> mldProductRecodeListLoadState = new MutableLiveData<>();
    public MutableLiveData<LoadState> mldServicePackInfoLoadState = new MutableLiveData<>();

    public MainViewModel(DataRepository dataRepository) {
        this.dataRepository = dataRepository;
    }

    public void reqProductListBySkuIds(List<String> skuIdList) {
        dataRepository.getProductListBySkuIds(new ProductListBySkuIdsReq(skuIdList))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(MainViewModel.this)
                .subscribe(new DisposableObserver<BaseResp<List<ProductListMo.Record>>>() {
                    @Override
                    public void onNext(@NonNull BaseResp<List<ProductListMo.Record>> response) {
                        List<ProductListMo.Record> newList = response.data;
                        newList.forEach(record -> record.setScore(getRandomString(8, 10)));
                        productRecodeList.addAll(newList);
                        mldProductRecodeListLoadState.setValue(LoadState.Success);
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

    public void reqServicePackInfo() {
        mldServicePackInfoLoadState.setValue(LoadState.Loading);
        dataRepository.getServicePackInfo(CommonUtils.getDeviceSn())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(this)
                .subscribe(new DisposableObserver<BaseResp<ServicePkgInfoResp>>() {
                    @Override
                    public void onNext(@NonNull BaseResp<ServicePkgInfoResp> response) {
                        if (response.code == BaseConstants.API_HANDLE_SUCCESS) {
                            globalLayout = GlobalLayoutHelper.analysisGlobalLayout(response.data.layoutJson);
                            mldServicePackInfoLoadState.setValue(LoadState.Success);
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        mldServicePackInfoLoadState.setValue(LoadState.Failed);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    @SuppressLint("DefaultLocale")
    public String getRandomString(int startNum, int endNum) {
        double newNum = startNum + Math.random() * (endNum - startNum);
        return String.format("%.1f", newNum);
    }
}
