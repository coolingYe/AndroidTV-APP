package com.zee.launcher.home.data;


import com.zee.launcher.home.data.model.MainCategoryMo;
import com.zee.launcher.home.data.model.ProductListMo;
import com.zee.launcher.home.data.protocol.request.CollectReq;
import com.zee.launcher.home.data.protocol.request.CoursewareStartReq;
import com.zee.launcher.home.data.protocol.request.MainCategoryReq;
import com.zee.launcher.home.data.protocol.request.ProDetailReq;
import com.zee.launcher.home.data.protocol.request.ProductListBySkuIdsReq;
import com.zee.launcher.home.data.protocol.request.ProductListReq;
import com.zee.launcher.home.data.protocol.request.ProductModuleListReq;
import com.zee.launcher.home.data.protocol.request.ProductRecommendReq;
import com.zee.launcher.home.data.protocol.request.PublishReq;
import com.zee.launcher.home.data.protocol.request.RemoveCollectReq;
import com.zee.launcher.home.data.protocol.request.TouristLoginReq;
import com.zee.launcher.home.data.protocol.request.UpgradeReq;
import com.zee.launcher.home.data.protocol.request.UploadLogReq;
import com.zee.launcher.home.data.protocol.response.AkSkResp;
import com.zee.launcher.home.data.protocol.response.CollectResp;
import com.zee.launcher.home.data.protocol.response.FavoriteStateResp;
import com.zee.launcher.home.data.protocol.response.ProDetailResp;
import com.zee.launcher.home.data.protocol.response.PublishResp;
import com.zee.launcher.home.data.protocol.response.ServicePkgInfoResp;
import com.zee.launcher.home.data.protocol.response.UpgradeResp;
import com.zee.launcher.home.data.source.http.service.ApiService;
import com.zeewain.base.data.protocol.response.BaseResp;
import com.zeewain.base.utils.RetrofitClient;
import com.zeewain.base.data.protocol.request.AkSkReq;
import com.zeewain.base.data.protocol.response.LoginResp;

import java.util.List;

import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.http.Body;

public class DataRepository implements ApiService {

    private static volatile DataRepository instance;
    private final ApiService apiService;

    private DataRepository(ApiService apiService){
        this.apiService = apiService;
    }

    public static DataRepository getInstance(){
        if(instance == null){
            synchronized (DataRepository.class){
                if (instance == null){
                    instance = new DataRepository(RetrofitClient.getInstance().create(ApiService.class));
                }
            }
        }
        return instance;
    }

    public Observable<BaseResp<List<MainCategoryMo>>> getMainCategoryList(MainCategoryReq mainCategoryReq){
        return apiService.getMainCategoryList(mainCategoryReq);
    }

    public Observable<BaseResp<ProductListMo>> getProductList(ProductListReq productListReq){
        return apiService.getProductList(productListReq);
    }

    @Override
    public Observable<BaseResp<List<ProductListMo.Record>>> getProductListBySkuIds(ProductListBySkuIdsReq skuIds) {
        return apiService.getProductListBySkuIds(skuIds);
    }

    public Observable<BaseResp<ProductListMo>> getProductModuleList(ProductModuleListReq productModuleListReq){
        return apiService.getProductModuleList(productModuleListReq);
    }

    public Observable<BaseResp<ProDetailResp>> getProDetailInfo(@Body ProDetailReq proDetailReq){
        return apiService.getProDetailInfo(proDetailReq);
    }

    public Observable<BaseResp<PublishResp>> getPublishedVersionInfo(@Body PublishReq publishReq){
        return apiService.getPublishedVersionInfo(publishReq);
    }

    public Observable<BaseResp<UpgradeResp>> getUpgradeVersionInfo(@Body UpgradeReq upgradeReq){
        return apiService.getUpgradeVersionInfo(upgradeReq);
    }

    public Observable<BaseResp<FavoriteStateResp>> getFavoriteState(String objId) {
        return  apiService.getFavoriteState(objId);
    }

    public Observable<BaseResp<CollectResp>> addFavorites(@Body CollectReq collectReq){
        return apiService.addFavorites(collectReq);
    }

    public  Observable<BaseResp<String>> removeFavorites(@Body RemoveCollectReq removeCollectReq){
        return apiService.removeFavorites(removeCollectReq);
    }

    public Observable<BaseResp<String>> uploadLog(UploadLogReq uploadLogReq) {
        return apiService.uploadLog(uploadLogReq);
    }

    public Observable<BaseResp<String>> startCourseware(CoursewareStartReq coursewareStartReq){
        return apiService.startCourseware(coursewareStartReq);
    }

    @Override
    public Observable<BaseResp<ServicePkgInfoResp>> getServicePackInfo(String deviceSn) {
        return apiService.getServicePackInfo(deviceSn);
    }

    @Override
    public Observable<BaseResp<List<ProductListMo.Record>>> getProductRecommend(ProductRecommendReq productRecommendReq) {
        return apiService.getProductRecommend(productRecommendReq);
    }

    @Override
    public Observable<BaseResp<String>> getDeviceHealth(String deviceSn) {
        return apiService.getDeviceHealth(deviceSn);
    }

    @Override
    public Observable<BaseResp<String>> getDeviceOffline(String deviceSn) {
        return apiService.getDeviceOffline(deviceSn);
    }

    public Observable<ResponseBody> downloadSmallFile(String url){
        return apiService.downloadSmallFile(url);
    }





    @Override
    public Observable<BaseResp<LoginResp>> touristLoginReq(TouristLoginReq touristLoginReq) {
        return apiService.touristLoginReq(touristLoginReq);
    }


    public Observable<BaseResp<AkSkResp>> getAkSkInfo(AkSkReq akSkReq){
        return apiService.getAkSkInfo(akSkReq);
    }
}
