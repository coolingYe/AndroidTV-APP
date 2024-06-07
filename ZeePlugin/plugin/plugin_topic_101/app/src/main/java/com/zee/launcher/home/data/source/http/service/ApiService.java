package com.zee.launcher.home.data.source.http.service;


import com.google.gson.JsonObject;
import com.zee.launcher.home.data.model.MainCategoryMo;
import com.zee.launcher.home.data.model.ProductListMo;
import com.zee.launcher.home.data.protocol.request.CollectReq;
import com.zee.launcher.home.data.protocol.request.CoursewareStartReq;
import com.zee.launcher.home.data.protocol.request.MainCategoryReq;
import com.zee.launcher.home.data.protocol.request.OrganizerInfoListReq;
import com.zee.launcher.home.data.protocol.request.ProDetailReq;
import com.zee.launcher.home.data.protocol.request.ProductListBySkuIdsReq;
import com.zee.launcher.home.data.protocol.request.ProductListReq;
import com.zee.launcher.home.data.protocol.request.ProductModuleListReq;
import com.zee.launcher.home.data.protocol.request.ProductRecommendReq;
import com.zee.launcher.home.data.protocol.request.PublishReq;
import com.zee.launcher.home.data.protocol.request.RemoveCollectReq;
import com.zee.launcher.home.data.protocol.request.UpgradeReq;
import com.zee.launcher.home.data.protocol.request.UploadLogReq;
import com.zee.launcher.home.data.protocol.response.CollectResp;
import com.zee.launcher.home.data.protocol.response.FavoriteStateResp;
import com.zee.launcher.home.data.protocol.response.OrganizerInfoResp;
import com.zee.launcher.home.data.protocol.response.ProDetailResp;
import com.zee.launcher.home.data.protocol.response.PublishResp;
import com.zee.launcher.home.data.protocol.response.RankingResp;
import com.zee.launcher.home.data.protocol.response.ServicePkgInfoResp;
import com.zee.launcher.home.data.protocol.response.UpgradeResp;
import com.zeewain.base.config.BaseConstants;
import com.zeewain.base.data.protocol.response.BaseResp;
import com.zwn.user.data.protocol.request.AkSkReq;
import com.zwn.user.data.protocol.request.DeviceSNReq;
import com.zwn.user.data.protocol.request.SessionResultReq;
import com.zwn.user.data.protocol.response.AkSkResp;
import com.zwn.user.data.protocol.response.CreateSessionResp;
import com.zwn.user.data.protocol.response.LoginResp;
import com.zwn.user.data.protocol.response.UserInfoResp;

import java.util.List;

import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiService {

    @POST(BaseConstants.basePath + "/product/v2/category/list")
    Observable<BaseResp<List<MainCategoryMo>>> getMainCategoryList(@Body MainCategoryReq mainCategoryReq);

    @POST(BaseConstants.basePath + "/product/online/list")
    Observable<BaseResp<ProductListMo>> getProductList(@Body ProductListReq productListReq);

    @POST(BaseConstants.basePath + BaseConstants.ApiPath.PRODUCT_ONLINE_QUERY_LIST)
    Observable<BaseResp<List<ProductListMo.Record>>> getProductListBySkuIds(@Body ProductListBySkuIdsReq skuIds);

    @POST(BaseConstants.basePath + "/product/online/module")
    Observable<BaseResp<ProductListMo>> getProductModuleList(@Body ProductModuleListReq productModuleListReq);

    @POST(BaseConstants.basePath + BaseConstants.ApiPath.PRODUCT_DETAIL)
    Observable<BaseResp<ProDetailResp>> getProDetailInfo(@Body ProDetailReq proDetailReq);

    @POST(BaseConstants.basePath + BaseConstants.ApiPath.SW_VERSION_LATEST)
    Observable<BaseResp<PublishResp>> getPublishedVersionInfo(@Body PublishReq publishReq);

    @POST(BaseConstants.basePath + BaseConstants.ApiPath.SW_VERSION_NEWER)
    Observable<BaseResp<UpgradeResp>> getUpgradeVersionInfo(@Body UpgradeReq upgradeReq);

    @GET(BaseConstants.basePath + BaseConstants.ApiPath.USER_FAVORITES_ITEM_INFO)
    Observable<BaseResp<FavoriteStateResp>> getFavoriteState(@Query("objId") String objId);

    @POST(BaseConstants.basePath + "/ums/favorite/courseware/add")
    Observable<BaseResp<CollectResp>> addFavorites(@Body CollectReq collectReq);

    @POST(BaseConstants.basePath + "/ums/favorite/courseware/del")
    Observable<BaseResp<String>> removeFavorites(@Body RemoveCollectReq removeCollectReq);

    @POST(BaseConstants.basePath + "/logcollection/log/upload")
    Observable<BaseResp<String>> uploadLog(@Body UploadLogReq uploadLogReq);

    @POST(BaseConstants.basePath + "/ums/playHistory/add")
    Observable<BaseResp<String>> startCourseware(@Body CoursewareStartReq coursewareStartReq);

    @GET(BaseConstants.basePath + BaseConstants.ApiPath.SERVICE_PACKAGE_INFO)
    Observable<BaseResp<ServicePkgInfoResp>> getServicePackInfo(@Query("deviceSn") String deviceSn);

    @POST(BaseConstants.basePath + BaseConstants.ApiPath.PRODUCT_RECOMMEND_LIST)
    Observable<BaseResp<List<ProductListMo.Record>>> getProductRecommend(@Body ProductRecommendReq productRecommendReq);

    @GET(BaseConstants.basePath + "/dmsmgr/purchase/device/health")
    Observable<BaseResp<String>> getDeviceHealth(@Query("deviceSn") String deviceSn);

    @GET(BaseConstants.basePath + "/dmsmgr/purchase/device/offline")
    Observable<BaseResp<String>> getDeviceOffline(@Query("deviceSn") String deviceSn);

    @POST(BaseConstants.basePath + "/metamatch/ranking/list")
    Observable<BaseResp<RankingResp>> getRankingInfo(@Body JsonObject jsonObject);

    @GET(BaseConstants.basePath + "/dmsmgr/organize/user/info")
    Observable<BaseResp<UserInfoResp>> getUserInfo();

    @POST(BaseConstants.basePath + "/metamatch/organizerInfo/list")
    Observable<BaseResp<List<OrganizerInfoResp>>> getOrganizerInfoList(@Body OrganizerInfoListReq organizerInfoListReq);

    @POST(BaseConstants.basePath + "/auth/client/get-ak-sk")
    Observable<BaseResp<AkSkResp>> getAkSkInfo(@Body AkSkReq akSkReq);

    @POST(BaseConstants.basePath + "/operation/sso/session/create")
    Observable<BaseResp<CreateSessionResp>> createSession(@Body DeviceSNReq deviceSNReq);

    @Headers({"CONNECT_TIMEOUT:5000", "READ_TIMEOUT:65000", "WRITE_TIMEOUT:5000"})
    @POST(BaseConstants.basePath + "/operation/sso/session/result/get")
    Observable<BaseResp<LoginResp>> getSessionResult(@Body SessionResultReq sessionIdReq);
}
