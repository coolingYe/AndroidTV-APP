package com.zee.launcher.home;


import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.alibaba.fastjson.JSONArray;
import com.google.gson.Gson;
import com.zee.launcher.home.config.ProdConstants;
import com.zee.launcher.home.data.DataRepository;
import com.zee.launcher.home.data.layout.GlobalLayout;
import com.zee.launcher.home.data.model.ProductListMo;
import com.zee.launcher.home.data.model.ProductRecordListLoadState;
import com.zee.launcher.home.data.model.ProductSkuIdListType;
import com.zee.launcher.home.data.protocol.request.ProductListBySkuIdsReq;
import com.zee.launcher.home.data.protocol.response.AdvertBean;
import com.zee.launcher.home.data.protocol.response.ServicePkgInfoResp;
import com.zee.launcher.home.utils.GlobalLayoutHelper;
import com.zee.paged.model.PagedListLoadState;
import com.zeewain.base.config.BaseConstants;
import com.zeewain.base.config.SharePrefer;
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

    public List<ProductListMo.Record> getProductRecodeListFromCache(String mapKey){
        return productRecodeListMap.get(mapKey);
    }

    public boolean isExistCacheProductRecodeList(String careKey){
        if(productRecodeListMap.get(careKey) != null){
            return true;
        }
        return false;
    }

    public void reqProductListBySkuIds(List<String> skuIdList, final String careKey){
        if(skuIdList.size() > ProdConstants.PRD_PAGE_SIZE){
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

    public List<String> getAppSkusByPageIndex(int pageIndex) {
        if(globalLayout.layout.pages.size() > pageIndex) {
            AdvertBean advertPage = new AdvertBean();
            JSONArray content = globalLayout.layout.pages.get(pageIndex).content;
            if (content != null && content.size() > 0) {
                for (int i = 0; i < content.size(); i++) {
                    String uid = content.getJSONObject(i).getString("uid");
                    if (uid.equals("mixed-swiper")) {
                        JSONArray jsonArray = content.getJSONObject(0).getJSONObject("config").getJSONArray("items");
                        for (int j = 0; j < jsonArray.size(); j++) {
                            String kind = jsonArray.getJSONObject(j).getString("kind");
                            if (kind.equals("app")) {
                                String skuId = jsonArray.getJSONObject(j).getString("skuId");
                                advertPage.config.appSkus.add(skuId);
                            }
                        }
                    } else {
                        AdvertBean advertPageList = new Gson().fromJson(String.valueOf(content.getJSONObject(i)), AdvertBean.class);
                        if (advertPageList.config != null && advertPageList.config.appSkus != null) {
                            advertPage.config.appSkus.addAll(advertPageList.config.appSkus);
                        }
                    }
                }
                if (advertPage.config.appSkus.size() > 0) {
                    return advertPage.config.appSkus;
                }
            }
        }

        return new ArrayList<>();
    }

    public List<ProductSkuIdListType> getProductSkuIdListTypeList() {
        List<ProductSkuIdListType> productSkuIdListTypeList = new ArrayList<>();
        for (int i = 0; i < globalLayout.layout.pages.size(); i++) {
            productSkuIdListTypeList.add(new ProductSkuIdListType(String.valueOf(i), getAppSkusByPageIndex(i)));
        }
        return productSkuIdListTypeList;
    }

    public boolean reqProductListBySkuIdsPaged(List<String> skuIdList, final String moduleType){
        if(reqProductRecodeListMap.containsKey(moduleType)){
            return false;
        }
        reqProductRecodeListMap.put(moduleType, moduleType);

        final List<ProductListMo.Record> productRecordListMo = productRecodeListMap.get(moduleType);
        int pageNum = 1;
        if(productRecordListMo != null){
            if(productRecordListMo.size() ==  skuIdList.size()){// loaded Done;
                reqProductRecodeListMap.remove(moduleType);

                if(productRecordListMo.size() % ProdConstants.PRD_PAGE_SIZE != 0){
                    pageNum = (productRecordListMo.size() / ProdConstants.PRD_PAGE_SIZE + 1);
                }else{
                    pageNum = (productRecordListMo.size() / ProdConstants.PRD_PAGE_SIZE);
                }
                mldModuleListPagedLoadState.setValue(new PagedListLoadState<>(moduleType, LoadState.Success, pageNum, new ArrayList<>()));
                return false;
            }
            pageNum = productRecordListMo.size() / ProdConstants.PRD_PAGE_SIZE + 1;
        }
        final int reqPageNum = pageNum;
        int startIndex = (reqPageNum -1) * ProdConstants.PRD_PAGE_SIZE;
        int endIndex = reqPageNum * ProdConstants.PRD_PAGE_SIZE;
        if(endIndex > skuIdList.size()){
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

    public void reqServicePackInfo(){
        mldServicePackInfoLoadState.setValue(LoadState.Loading);
        dataRepository.getServicePackInfo(deviceSn)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(this)
                .subscribe(new DisposableObserver<BaseResp<ServicePkgInfoResp>>() {
                    @Override
                    public void onNext(@NonNull BaseResp<ServicePkgInfoResp> response) {
                        if(response.code == BaseConstants.API_HANDLE_SUCCESS){
                            globalLayout = GlobalLayoutHelper.analysisGlobalLayout(response.data.themeJson);
                            if(globalLayout == null){
                                mldServicePackInfoLoadState.setValue(LoadState.Failed);
                                mldToastMsg.setValue("服务包配置错误！");
                            }else{
                                mldServicePackInfoLoadState.setValue(LoadState.Success);
                                SharePrefer.displayName = globalLayout.layout.pages.get(0).config.fullName;
                            }
                        }else{
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
