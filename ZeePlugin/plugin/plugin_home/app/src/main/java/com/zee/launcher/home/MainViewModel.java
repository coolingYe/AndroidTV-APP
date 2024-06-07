package com.zee.launcher.home;


import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.zee.launcher.home.config.ProdConstants;
import com.zee.launcher.home.data.DataRepository;
import com.zee.launcher.home.data.layout.AppCardListLayout;
import com.zee.launcher.home.data.layout.GlobalLayout;
import com.zee.launcher.home.data.layout.PageLayoutDTO;
import com.zee.launcher.home.data.layout.SwiperLayout;
import com.zee.launcher.home.data.model.ProductListMo;
import com.zee.launcher.home.data.model.ProductRecordListLoadState;
import com.zee.launcher.home.data.model.ProductSkuIdListType;
import com.zee.launcher.home.data.protocol.request.ProductListBySkuIdsReq;
import com.zee.launcher.home.data.protocol.response.ServicePkgInfoResp;
import com.zee.launcher.home.ui.home.model.ProductListType;
import com.zee.launcher.home.utils.GlobalLayoutHelper;
import com.zee.paged.model.PagedListLoadState;
import com.zeewain.base.config.BaseConstants;
import com.zeewain.base.data.protocol.response.BaseResp;
import com.zeewain.base.model.LoadState;
import com.zeewain.base.ui.BaseViewModel;
import com.zeewain.base.utils.CommonUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

public class MainViewModel extends BaseViewModel {
    private final DataRepository dataRepository;
    private final String deviceSn;

    public GlobalLayout globalLayout;
    private final HashMap<String, List<ProductListMo.Record>> productRecodeListMap = new HashMap<>();
    private final ConcurrentHashMap<String, String> reqProductRecodeListMap = new ConcurrentHashMap<>();//used for filter req;

    public MutableLiveData<Boolean> mldNetConnected = new MutableLiveData<>();
    public MutableLiveData<Integer> mldOnPause = new MutableLiveData<>();
    public MutableLiveData<Integer> mldOnResume = new MutableLiveData<>();
    public MutableLiveData<String> mldToastMsg = new MutableLiveData<>();
    public MutableLiveData<ProductRecordListLoadState> mldProductRecodeListLoadState = new MutableLiveData<>();
    public MutableLiveData<LoadState> mldServicePackInfoLoadState = new MutableLiveData<>();
    public MutableLiveData<PagedListLoadState<ProductListMo.Record>> mldModuleListPagedLoadState = new MutableLiveData<>();

    public MainViewModel(DataRepository dataRepository) {
        this.dataRepository = dataRepository;
        this.deviceSn = CommonUtils.getDeviceSn();
    }

    public List<ProductListMo.Record> getProductRecodeListFromCache(String mapKey) {
        return productRecodeListMap.get(mapKey);
    }

    public boolean isExistCacheProductRecodeList(String careKey) {
        return productRecodeListMap.get(careKey) != null;
    }

    public List<ProductListType> getProductListTypeList(int pageIndex) {
        List<ProductListType> typeList = new ArrayList<>();
        PageLayoutDTO pageLayoutDTO = globalLayout.layout.pages.get(pageIndex);
        if (pageLayoutDTO.swiperLayout != null) {
            ProductListType productListType = new ProductListType(ProductListType.TYPE_BANNER, pageIndex + "_banner", "banner");
            productListType.swiperLayout = pageLayoutDTO.swiperLayout;
            if (pageLayoutDTO.swiperLayout.uid.equals("mixed-swiper")) {
                if (productListType.swiperLayout.config.items.size() > 0) {
                    productListType.swiperLayout.config.appSkus = new ArrayList<>();
                    for (SwiperLayout.Item item : productListType.swiperLayout.config.items) {
                        if ("app".equals(item.kind)) {
                            productListType.swiperLayout.config.appSkus.add(item.skuId);
                        }
                    }
                    typeList.add(productListType);
                }
            } else {
                if (productListType.swiperLayout.config.appSkus.size() > 0)
                    typeList.add(productListType);
            }
        }
        for (int i = 0; i < pageLayoutDTO.appCardListLayoutList.size(); i++) {
            AppCardListLayout appCardListLayout = pageLayoutDTO.appCardListLayoutList.get(i);
            ProductListType productListType = new ProductListType(ProductListType.TYPE_CLASSIC_MODULE, pageIndex + "_" + i, appCardListLayout.config.title);
            productListType.appCardListLayout = appCardListLayout;
            if (productListType.appCardListLayout.config.appSkus.size() > 0)
                typeList.add(productListType);
        }
        return typeList;
    }

    public ArrayList<String> getAllBannerImageList() {
        ArrayList<String> dataList = new ArrayList<>();
        for (int i = 0; i < globalLayout.layout.pages.size(); i++) {
            PageLayoutDTO pageLayoutDTO = globalLayout.layout.pages.get(i);
            if (pageLayoutDTO.swiperLayout != null) {
                if (pageLayoutDTO.swiperLayout.uid.equals("mixed-swiper")) {
                    for (SwiperLayout.Item item : pageLayoutDTO.swiperLayout.config.items) {
                        if ("img".equals(item.kind)) {
                            dataList.add(item.url);
                        }
                    }
                }
            }
        }
        return dataList;
    }

    public ArrayList<String> getAllBannerVideoList() {
        ArrayList<String> dataList = new ArrayList<>();
        for (int i = 0; i < globalLayout.layout.pages.size(); i++) {
            PageLayoutDTO pageLayoutDTO = globalLayout.layout.pages.get(i);
            if (pageLayoutDTO.swiperLayout != null) {
                if (pageLayoutDTO.swiperLayout.uid.equals("mixed-swiper")) {
                    for (SwiperLayout.Item item : pageLayoutDTO.swiperLayout.config.items) {
                        if ("video".equals(item.kind)) {
                            dataList.add(item.url);
                        }
                    }
                }
            }
        }
        return dataList;
    }

    public List<ProductSkuIdListType> getProductSkuIdListTypeList() {
        List<ProductSkuIdListType> productSkuIdListTypeList = new ArrayList<>();
        for (int i = 0; i < globalLayout.layout.pages.size(); i++) {
            List<ProductListType> productListTypeList = getProductListTypeList(i);
            for (ProductListType productListType : productListTypeList) {
                if (productListType.type == ProductListType.TYPE_BANNER) {
                    productSkuIdListTypeList.add(new ProductSkuIdListType(productListType.careKey, productListType.swiperLayout.config.appSkus));
                } else if (productListType.type == ProductListType.TYPE_CLASSIC_MODULE) {
                    productSkuIdListTypeList.add(new ProductSkuIdListType(productListType.careKey, productListType.appCardListLayout.config.appSkus));
                }
            }
        }
        return productSkuIdListTypeList;
    }

    public void reqProductListBySkuIds(List<String> skuIdList, final String careKey) {
        if (skuIdList.size() > ProdConstants.PRD_PAGE_SIZE) {
            skuIdList = skuIdList.subList(0, ProdConstants.PRD_PAGE_SIZE);
        }
        dataRepository.getProductListBySkuIds(new ProductListBySkuIdsReq(skuIdList, deviceSn))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(MainViewModel.this)
                .subscribe(new DisposableObserver<BaseResp<List<ProductListMo.Record>>>() {
                    @Override
                    public void onNext(@NonNull BaseResp<List<ProductListMo.Record>> response) {
                        productRecodeListMap.put(careKey, response.data);
                        mldProductRecodeListLoadState.setValue(new ProductRecordListLoadState(careKey, LoadState.Success));
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        mldProductRecodeListLoadState.setValue(new ProductRecordListLoadState(careKey, LoadState.Failed));
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    public boolean reqProductListBySkuIdsPaged(List<String> skuIdList, final String moduleType) {
        if (reqProductRecodeListMap.containsKey(moduleType)) {
            return false;
        }
        reqProductRecodeListMap.put(moduleType, moduleType);

        final List<ProductListMo.Record> productRecordListMo = productRecodeListMap.get(moduleType);
        int pageNum = 1;
        if (productRecordListMo != null) {
            if (productRecordListMo.size() == skuIdList.size()) {// loaded Done;
                reqProductRecodeListMap.remove(moduleType);

                if (productRecordListMo.size() % ProdConstants.PRD_PAGE_SIZE != 0) {
                    pageNum = (productRecordListMo.size() / ProdConstants.PRD_PAGE_SIZE + 1);
                } else {
                    pageNum = (productRecordListMo.size() / ProdConstants.PRD_PAGE_SIZE);
                }
                mldModuleListPagedLoadState.setValue(new PagedListLoadState<>(moduleType, LoadState.Success, pageNum, new ArrayList<>()));
                return false;
            }
            pageNum = productRecordListMo.size() / ProdConstants.PRD_PAGE_SIZE + 1;
        }
        final int reqPageNum = pageNum;
        int startIndex = (reqPageNum - 1) * ProdConstants.PRD_PAGE_SIZE;
        int endIndex = reqPageNum * ProdConstants.PRD_PAGE_SIZE;
        if (endIndex > skuIdList.size()) {
            endIndex = skuIdList.size();
        }
        List<String> usedSkuIdList = skuIdList.subList(startIndex, endIndex);
        mldModuleListPagedLoadState.setValue(new PagedListLoadState<>(moduleType, LoadState.Loading, reqPageNum));
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                dataRepository.getProductListBySkuIds(new ProductListBySkuIdsReq(usedSkuIdList, deviceSn))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnSubscribe(MainViewModel.this)
                        .subscribe(new DisposableObserver<BaseResp<List<ProductListMo.Record>>>() {
                            @Override
                            public void onNext(@NonNull BaseResp<List<ProductListMo.Record>> response) {
                                reqProductRecodeListMap.remove(moduleType);
                                List<ProductListMo.Record> productRecordListMo = productRecodeListMap.get(moduleType);
                                if (productRecordListMo == null) {
                                    productRecodeListMap.put(moduleType, response.data);
                                } else {
                                    productRecordListMo.addAll(response.data);
                                }
                                mldModuleListPagedLoadState.setValue(new PagedListLoadState<>(moduleType, LoadState.Success, reqPageNum, response.data));
                            }

                            @Override
                            public void onError(@NonNull Throwable e) {
                                reqProductRecodeListMap.remove(moduleType);
                                mldModuleListPagedLoadState.setValue(new PagedListLoadState<>(moduleType, LoadState.Failed, reqPageNum));
                            }

                            @Override
                            public void onComplete() {

                            }
                        });
            }
        }, 600);

        return true;
    }

    public void reqServicePackInfo() {
        mldServicePackInfoLoadState.setValue(LoadState.Loading);
        dataRepository.getServicePackInfo(deviceSn)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(this)
                .subscribe(new DisposableObserver<BaseResp<ServicePkgInfoResp>>() {
                    @Override
                    public void onNext(@NonNull BaseResp<ServicePkgInfoResp> response) {
                        if (response.code == BaseConstants.API_HANDLE_SUCCESS) {
                            globalLayout = GlobalLayoutHelper.analysisGlobalLayout(response.data.themeJson);
                            if (globalLayout == null) {
                                mldServicePackInfoLoadState.setValue(LoadState.Failed);
                                mldToastMsg.setValue("服务包配置错误！");
                            } else {
                                mldServicePackInfoLoadState.setValue(LoadState.Success);
                            }
                        } else {
                            mldToastMsg.setValue(response.message);
                            mldServicePackInfoLoadState.setValue(LoadState.Failed);
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
}
